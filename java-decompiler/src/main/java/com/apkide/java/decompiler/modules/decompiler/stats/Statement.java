/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.apkide.java.decompiler.modules.decompiler.stats;


import androidx.annotation.NonNull;

import com.apkide.java.decompiler.code.InstructionSequence;
import com.apkide.java.decompiler.main.DecompilerContext;
import com.apkide.java.decompiler.main.collectors.BytecodeMappingTracer;
import com.apkide.java.decompiler.main.collectors.CounterContainer;
import com.apkide.java.decompiler.modules.decompiler.StatEdge;
import com.apkide.java.decompiler.modules.decompiler.StrongConnectivityHelper;
import com.apkide.java.decompiler.modules.decompiler.exps.Exprent;
import com.apkide.java.decompiler.struct.match.IMatchable;
import com.apkide.java.decompiler.struct.match.MatchEngine;
import com.apkide.java.decompiler.struct.match.MatchNode;
import com.apkide.java.decompiler.util.TextBuffer;
import com.apkide.java.decompiler.util.VBStyleCollection;
import com.apkide.java.decompiler.code.CodeConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class Statement implements IMatchable {
  public StatementType type;
  public int id;

  private final Map<StatEdge.EdgeType, List<StatEdge>> mapSuccEdges = new HashMap<>();
  private final Map<StatEdge.EdgeType, List<StatEdge>> mapPredEdges = new HashMap<>();

  private final Map<StatEdge.EdgeType, List<Statement>> mapSuccStates = new HashMap<>();
  private final Map<StatEdge.EdgeType, List<Statement>> mapPredStates = new HashMap<>();

  private final HashSet<StatEdge> labelEdges = new HashSet<>();
  // copied statement, s. deobfuscating of irreducible CFGs
  private boolean copied = false;
  // statement as graph
  protected final VBStyleCollection<Statement, Integer> stats = new VBStyleCollection<>();
  protected Statement parent;
  protected Statement first;
  protected List<Exprent> exprents;
  protected final List<Exprent> varDefinitions = new ArrayList<>();
  // relevant for the first stage of processing only
  // set to null after initializing of the statement structure
  protected Statement post;
  protected StatementType lastBasicType = StatementType.GENERAL;
  protected boolean isMonitorEnter;
  protected boolean containsMonitorExit;
  protected HashSet<Statement> continueSet = new HashSet<>();

  {
    id = DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER);
  }

  Statement(@NonNull StatementType type) {
    this.type = type;
  }

  Statement(@NonNull StatementType type, int id) {
    this.type = type;
    this.id = id;
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  public void clearTempInformation() {

    post = null;
    continueSet = null;

    copied = false;
    // FIXME: used in FlattenStatementsHelper.flattenStatement()! check and remove
    //lastBasicType = LASTBASICTYPE_GENERAL;
    isMonitorEnter = false;
    containsMonitorExit = false;

    processMap(mapSuccEdges);
    processMap(mapPredEdges);
    processMap(mapSuccStates);
    processMap(mapPredStates);
  }

  private static <T> void processMap(Map<StatEdge.EdgeType, List<T>> map) {
    map.remove(StatEdge.EdgeType.EXCEPTION);

    List<T> lst = map.get(StatEdge.EdgeType.DIRECT_ALL);
    if (lst != null) {
      map.put(StatEdge.EdgeType.ALL, new ArrayList<>(lst));
    }
    else {
      map.remove(StatEdge.EdgeType.ALL);
    }
  }

  public void collapseNodesToStatement(Statement stat) {

    Statement head = stat.getFirst();
    Statement post = stat.getPost();

    VBStyleCollection<Statement, Integer> setNodes = stat.getStats();

    // post edges
    if (post != null) {
      for (StatEdge edge : post.getEdges(StatEdge.EdgeType.DIRECT_ALL, StatEdge.EdgeDirection.BACKWARD)) {
        if (stat.containsStatementStrict(edge.getSource())) {
          edge.getSource().changeEdgeType(StatEdge.EdgeDirection.FORWARD, edge, StatEdge.EdgeType.BREAK);
          stat.addLabeledEdge(edge);
        }
      }
    }

    // regular head edges
    for (StatEdge prededge : head.getAllPredecessorEdges()) {

      if (prededge.getType() != StatEdge.EdgeType.EXCEPTION &&
          stat.containsStatementStrict(prededge.getSource())) {
        prededge.getSource().changeEdgeType(StatEdge.EdgeDirection.FORWARD, prededge, StatEdge.EdgeType.CONTINUE);
        stat.addLabeledEdge(prededge);
      }

      head.removePredecessor(prededge);
      prededge.getSource().changeEdgeNode(StatEdge.EdgeDirection.FORWARD, prededge, stat);
      stat.addPredecessor(prededge);
    }

    if (setNodes.containsKey(first.id)) {
      first = stat;
    }

    // exception edges
    Set<Statement> setHandlers = new HashSet<>(head.getNeighbours(StatEdge.EdgeType.EXCEPTION, StatEdge.EdgeDirection.FORWARD));
    for (Statement node : setNodes) {
      setHandlers.retainAll(node.getNeighbours(StatEdge.EdgeType.EXCEPTION, StatEdge.EdgeDirection.FORWARD));
    }

    if (!setHandlers.isEmpty()) {

      for (StatEdge edge : head.getEdges(StatEdge.EdgeType.EXCEPTION, StatEdge.EdgeDirection.FORWARD)) {
        Statement handler = edge.getDestination();

        if (setHandlers.contains(handler)) {
          if (!setNodes.containsKey(handler.id)) {
            stat.addSuccessor(new StatEdge(stat, handler, edge.getExceptions()));
          }
        }
      }

      for (Statement node : setNodes) {
        for (StatEdge edge : node.getEdges(StatEdge.EdgeType.EXCEPTION, StatEdge.EdgeDirection.FORWARD)) {
          if (setHandlers.contains(edge.getDestination())) {
            node.removeSuccessor(edge);
          }
        }
      }
    }

    if (post != null &&
        !stat.getNeighbours(StatEdge.EdgeType.EXCEPTION, StatEdge.EdgeDirection.FORWARD).contains(post)) { // TODO: second condition redundant?
      stat.addSuccessor(new StatEdge(StatEdge.EdgeType.REGULAR, stat, post));
    }


    // adjust statement collection
    for (Statement st : setNodes) {
      stats.removeWithKey(st.id);
    }

    stats.addWithKey(stat, stat.id);

    stat.setAllParent();
    stat.setParent(this);

    stat.buildContinueSet();
    // monitorenter and monitorexit
    stat.buildMonitorFlags();

    if (stat.type == StatementType.SWITCH) {
      // special case switch, sorting leaf nodes
      ((SwitchStatement)stat).sortEdgesAndNodes();
    }
  }

  public void setAllParent() {
    for (Statement st : stats) {
      st.setParent(this);
    }
  }

  public void addLabeledEdge(StatEdge edge) {

    if (edge.closure != null) {
      edge.closure.getLabelEdges().remove(edge);
    }
    edge.closure = this;
    this.getLabelEdges().add(edge);
  }

  private void addEdgeDirectInternal(StatEdge.EdgeDirection direction, StatEdge edge, StatEdge.EdgeType edgetype) {
    Map<StatEdge.EdgeType, List<StatEdge>> mapEdges = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredEdges : mapSuccEdges;
    Map<StatEdge.EdgeType, List<Statement>> mapStates = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredStates : mapSuccStates;

    mapEdges.computeIfAbsent(edgetype, k -> new ArrayList<>()).add(edge);

    mapStates.computeIfAbsent(edgetype, k -> new ArrayList<>()).add(direction == StatEdge.EdgeDirection.BACKWARD ? edge.getSource() : edge.getDestination());
  }

  private void addEdgeInternal(StatEdge.EdgeDirection direction, StatEdge edge) {
    StatEdge.EdgeType type = edge.getType();

    StatEdge.EdgeType[] arrtypes;
    if (type == StatEdge.EdgeType.EXCEPTION) {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.EXCEPTION};
    }
    else {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.DIRECT_ALL, type};
    }

    for (StatEdge.EdgeType edgetype : arrtypes) {
      addEdgeDirectInternal(direction, edge, edgetype);
    }
  }

  private void removeEdgeDirectInternal(StatEdge.EdgeDirection direction, StatEdge edge, StatEdge.EdgeType edgetype) {

    Map<StatEdge.EdgeType, List<StatEdge>> mapEdges = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredEdges : mapSuccEdges;
    Map<StatEdge.EdgeType, List<Statement>> mapStates = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredStates : mapSuccStates;

    List<StatEdge> lst = mapEdges.get(edgetype);
    if (lst != null) {
      int index = lst.indexOf(edge);
      if (index >= 0) {
        lst.remove(index);
        mapStates.get(edgetype).remove(index);
      }
    }
  }

  private void removeEdgeInternal(StatEdge.EdgeDirection direction, StatEdge edge) {

    StatEdge.EdgeType type = edge.getType();

    StatEdge.EdgeType[] arrtypes;
    if (type == StatEdge.EdgeType.EXCEPTION) {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.EXCEPTION};
    }
    else {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.DIRECT_ALL, type};
    }

    for (StatEdge.EdgeType edgetype : arrtypes) {
      removeEdgeDirectInternal(direction, edge, edgetype);
    }
  }

  public void addPredecessor(StatEdge edge) {
    addEdgeInternal(StatEdge.EdgeDirection.BACKWARD, edge);
  }

  public void removePredecessor(StatEdge edge) {

    if (edge == null) {  // FIXME: redundant?
      return;
    }

    removeEdgeInternal(StatEdge.EdgeDirection.BACKWARD, edge);
  }

  public void addSuccessor(StatEdge edge) {
    addEdgeInternal(StatEdge.EdgeDirection.FORWARD, edge);

    if (edge.closure != null) {
      edge.closure.getLabelEdges().add(edge);
    }

    edge.getDestination().addPredecessor(edge);
  }

  public void removeSuccessor(StatEdge edge) {

    if (edge == null) {
      return;
    }

    removeEdgeInternal(StatEdge.EdgeDirection.FORWARD, edge);

    if (edge.closure != null) {
      edge.closure.getLabelEdges().remove(edge);
    }

    if (edge.getDestination() != null) {  // TODO: redundant?
      edge.getDestination().removePredecessor(edge);
    }
  }

  // TODO: make obsolete and remove
  public void removeAllSuccessors(Statement stat) {

    if (stat == null) {
      return;
    }

    for (StatEdge edge : getAllSuccessorEdges()) {
      if (edge.getDestination() == stat) {
        removeSuccessor(edge);
      }
    }
  }

  public HashSet<Statement> buildContinueSet() {
    continueSet.clear();

    for (Statement st : stats) {
      continueSet.addAll(st.buildContinueSet());
      if (st != first) {
        continueSet.remove(st.getBasichead());
      }
    }

    for (StatEdge edge : getEdges(StatEdge.EdgeType.CONTINUE, StatEdge.EdgeDirection.FORWARD)) {
      continueSet.add(edge.getDestination().getBasichead());
    }

    if (type == StatementType.DO) {
      continueSet.remove(first.getBasichead());
    }

    return continueSet;
  }

  public void buildMonitorFlags() {

    for (Statement st : stats) {
      st.buildMonitorFlags();
    }

    switch (type) {
      case BASIC_BLOCK:
        BasicBlockStatement bblock = (BasicBlockStatement)this;
        InstructionSequence seq = bblock.getBlock().getSeq();

        if (seq != null && seq.length() > 0) {
          for (int i = 0; i < seq.length(); i++) {
            if (seq.getInstr(i).opcode == CodeConstants.opc_monitorexit) {
              containsMonitorExit = true;
              break;
            }
          }
          isMonitorEnter = (seq.getLastInstr().opcode == CodeConstants.opc_monitorenter);
        }
        break;
      case SEQUENCE:
      case IF:
        containsMonitorExit = false;
        for (Statement st : stats) {
          containsMonitorExit |= st.isContainsMonitorExit();
        }

        break;
      case SYNCHRONIZED:
      case ROOT:
      case GENERAL:
        break;
      default:
        containsMonitorExit = false;
        for (Statement st : stats) {
          containsMonitorExit |= st.isContainsMonitorExit();
        }
    }
  }


  public List<Statement> getReversePostOrderList() {
    return getReversePostOrderList(first);
  }

  public List<Statement> getReversePostOrderList(Statement stat) {
    List<Statement> res = new ArrayList<>();

    addToReversePostOrderListIterative(stat, res);

    return res;
  }

  public List<Statement> getPostReversePostOrderList() {
    return getPostReversePostOrderList(null);
  }

  public List<Statement> getPostReversePostOrderList(List<Statement> lstexits) {

    List<Statement> res = new ArrayList<>();

    if (lstexits == null) {
      lstexits = new StrongConnectivityHelper(this).getExitReps();
    }

    HashSet<Statement> setVisited = new HashSet<>();

    for (Statement exit : lstexits) {
      addToPostReversePostOrderList(exit, res, setVisited);
    }

    if (res.size() != stats.size()) {
      throw new RuntimeException("computing post reverse post order failed!");
    }

    return res;
  }

  public boolean containsStatement(Statement stat) {
    return this == stat || containsStatementStrict(stat);
  }

  public boolean containsStatementStrict(Statement stat) {
    if (stats.contains(stat)) {
      return true;
    }

    for (Statement st : stats) {
      if (st.containsStatementStrict(stat)) {
        return true;
      }
    }

    return false;
  }

  public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
    throw new RuntimeException("not implemented");
  }

  // TODO: make obsolete and remove
  public List<Object> getSequentialObjects() {
    return new ArrayList<>(stats);
  }

  public void initExprents() {
    // do nothing
  }

  public void replaceExprent(Exprent oldexpr, Exprent newexpr) {
    // do nothing
  }

  public Statement getSimpleCopy() {
    throw new RuntimeException("not implemented");
  }

  public void initSimpleCopy() {
    if (!stats.isEmpty()) {
      first = stats.get(0);
    }
  }

  public void replaceStatement(Statement oldstat, Statement newstat) {

    for (StatEdge edge : oldstat.getAllPredecessorEdges()) {
      oldstat.removePredecessor(edge);
      edge.getSource().changeEdgeNode(StatEdge.EdgeDirection.FORWARD, edge, newstat);
      newstat.addPredecessor(edge);
    }

    for (StatEdge edge : oldstat.getAllSuccessorEdges()) {
      oldstat.removeSuccessor(edge);
      edge.setSource(newstat);
      newstat.addSuccessor(edge);
    }

    int statindex = stats.getIndexByKey(oldstat.id);
    stats.removeWithKey(oldstat.id);
    stats.addWithKeyAndIndex(statindex, newstat, newstat.id);

    newstat.setParent(this);
    newstat.post = oldstat.post;

    if (first == oldstat) {
      first = newstat;
    }

    List<StatEdge> lst = new ArrayList<>(oldstat.getLabelEdges());

    for (int i = lst.size() - 1; i >= 0; i--) {
      StatEdge edge = lst.get(i);
      if (edge.getSource() != newstat) {
        newstat.addLabeledEdge(edge);
      }
      else {
        if (this == edge.getDestination() || this.containsStatementStrict(edge.getDestination())) {
          edge.closure = null;
        }
        else {
          this.addLabeledEdge(edge);
        }
      }
    }

    oldstat.getLabelEdges().clear();
  }


  // *****************************************************************************
  // private methods
  // *****************************************************************************

  private static void addToReversePostOrderListIterative(Statement root, List<? super Statement> lst) {

    LinkedList<Statement> stackNode = new LinkedList<>();
    LinkedList<Integer> stackIndex = new LinkedList<>();
    HashSet<Statement> setVisited = new HashSet<>();

    stackNode.add(root);
    stackIndex.add(0);

    while (!stackNode.isEmpty()) {

      Statement node = stackNode.getLast();
      int index = stackIndex.removeLast();

      setVisited.add(node);

      List<StatEdge> lstEdges = node.getAllSuccessorEdges();

      for (; index < lstEdges.size(); index++) {
        StatEdge edge = lstEdges.get(index);
        Statement succ = edge.getDestination();

        if (!setVisited.contains(succ) &&
            (edge.getType() == StatEdge.EdgeType.REGULAR || edge.getType() == StatEdge.EdgeType.EXCEPTION)) { // TODO: edge filter?

          stackIndex.add(index + 1);

          stackNode.add(succ);
          stackIndex.add(0);

          break;
        }
      }

      if (index == lstEdges.size()) {
        lst.add(0, node);

        stackNode.removeLast();
      }
    }
  }


  private static void addToPostReversePostOrderList(Statement stat, List<? super Statement> lst, HashSet<? super Statement> setVisited) {

    if (setVisited.contains(stat)) { // because of not considered exception edges, s. isExitComponent. Should be rewritten, if possible.
      return;
    }
    setVisited.add(stat);

    for (StatEdge prededge : stat.getEdges(StatEdge.EdgeType.REGULAR.unite(StatEdge.EdgeType.EXCEPTION), StatEdge.EdgeDirection.BACKWARD)) {
      Statement pred = prededge.getSource();
      if (!setVisited.contains(pred)) {
        addToPostReversePostOrderList(pred, lst, setVisited);
      }
    }

    lst.add(0, stat);
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  public void changeEdgeNode(StatEdge.EdgeDirection direction, StatEdge edge, Statement value) {

    Map<StatEdge.EdgeType, List<StatEdge>> mapEdges = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredEdges : mapSuccEdges;
    Map<StatEdge.EdgeType, List<Statement>> mapStates = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredStates : mapSuccStates;

    StatEdge.EdgeType type = edge.getType();

    StatEdge.EdgeType[] arrtypes;
    if (type == StatEdge.EdgeType.EXCEPTION) {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.EXCEPTION};
    }
    else {
      arrtypes = new StatEdge.EdgeType[]{StatEdge.EdgeType.ALL, StatEdge.EdgeType.DIRECT_ALL, type};
    }

    for (StatEdge.EdgeType edgetype : arrtypes) {
      List<StatEdge> lst = mapEdges.get(edgetype);
      if (lst != null) {
        int index = lst.indexOf(edge);
        if (index >= 0) {
          mapStates.get(edgetype).set(index, value);
        }
      }
    }

    if (direction == StatEdge.EdgeDirection.BACKWARD) {
      edge.setSource(value);
    }
    else {
      edge.setDestination(value);
    }
  }

  public void changeEdgeType(StatEdge.EdgeDirection direction, StatEdge edge, StatEdge.EdgeType newtype) {

    StatEdge.EdgeType oldtype = edge.getType();
    if (oldtype == newtype) {
      return;
    }

    if (oldtype == StatEdge.EdgeType.EXCEPTION || newtype == StatEdge.EdgeType.EXCEPTION) {
      throw new RuntimeException("Invalid edge type!");
    }

    removeEdgeDirectInternal(direction, edge, oldtype);
    addEdgeDirectInternal(direction, edge, newtype);

    if (direction == StatEdge.EdgeDirection.FORWARD) {
      edge.getDestination().changeEdgeType(StatEdge.EdgeDirection.BACKWARD, edge, newtype);
    }

    edge.setType(newtype);
  }


  private List<StatEdge> getEdges(StatEdge.EdgeType type, @NonNull StatEdge.EdgeDirection direction) {

    Map<StatEdge.EdgeType, List<StatEdge>> map = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredEdges : mapSuccEdges;

    List<StatEdge> res;
    if ((type.mask() & (type.mask() - 1)) == 0) {
      res = map.get(type);
      res = res == null ? new ArrayList<>() : new ArrayList<>(res);
    }
    else {
      res = new ArrayList<>();
      for (StatEdge.EdgeType edgetype : StatEdge.EdgeType.types()) {
        if ((type.mask() & edgetype.mask()) != 0) {
          List<StatEdge> lst = map.get(edgetype);
          if (lst != null) {
            res.addAll(lst);
          }
        }
      }
    }

    return res;
  }

  public List<Statement> getNeighbours(StatEdge.EdgeType type, StatEdge.EdgeDirection direction) {

    Map<StatEdge.EdgeType, List<Statement>> map = direction == StatEdge.EdgeDirection.BACKWARD ? mapPredStates : mapSuccStates;

    List<Statement> res;
    if ((type.mask() & (type.mask() - 1)) == 0) {
      res = map.get(type);
      res = res == null ? new ArrayList<>() : new ArrayList<>(res);
    }
    else {
      res = new ArrayList<>();
      for (StatEdge.EdgeType edgetype : StatEdge.EdgeType.types()) {
        if ((type.mask() & edgetype.mask()) != 0) {
          List<Statement> lst = map.get(edgetype);
          if (lst != null) {
            res.addAll(lst);
          }
        }
      }
    }

    return res;
  }

  public Set<Statement> getNeighboursSet(StatEdge.EdgeType type, StatEdge.EdgeDirection direction) {
    return new HashSet<>(getNeighbours(type, direction));
  }

  public List<StatEdge> getSuccessorEdges(StatEdge.EdgeType type) {
    return getEdges(type, StatEdge.EdgeDirection.FORWARD);
  }

  public List<StatEdge> getPredecessorEdges(StatEdge.EdgeType type) {
    return getEdges(type, StatEdge.EdgeDirection.BACKWARD);
  }

  public List<StatEdge> getAllSuccessorEdges() {
    return getEdges(StatEdge.EdgeType.ALL, StatEdge.EdgeDirection.FORWARD);
  }

  public List<StatEdge> getAllPredecessorEdges() {
    return getEdges(StatEdge.EdgeType.ALL, StatEdge.EdgeDirection.BACKWARD);
  }

  public Statement getFirst() {
    return first;
  }

  public void setFirst(Statement first) {
    this.first = first;
  }

  public Statement getPost() {
    return post;
  }

  public VBStyleCollection<Statement, Integer> getStats() {
    return stats;
  }

  public StatementType getLastBasicType() {
    return lastBasicType;
  }

  public HashSet<Statement> getContinueSet() {
    return continueSet;
  }

  public boolean isContainsMonitorExit() {
    return containsMonitorExit;
  }

  public boolean isMonitorEnter() {
    return isMonitorEnter;
  }

  public BasicBlockStatement getBasichead() {
    if (type == StatementType.BASIC_BLOCK) {
      return (BasicBlockStatement)this;
    }
    else {
      return first.getBasichead();
    }
  }

  public boolean isLabeled() {

    for (StatEdge edge : labelEdges) {
      if (edge.labeled && edge.explicit) {  // FIXME: consistent setting
        return true;
      }
    }
    return false;
  }

  public boolean hasBasicSuccEdge() {

    // FIXME: default switch

    return type == StatementType.BASIC_BLOCK || (type == StatementType.IF &&
                                                        ((IfStatement)this).iftype == IfStatement.IFTYPE_IF) ||
           (type == StatementType.DO && ((DoStatement)this).getLoopType() != DoStatement.LoopType.DO);
  }


  public Statement getParent() {
    return parent;
  }

  public void setParent(Statement parent) {
    this.parent = parent;
  }

  public HashSet<StatEdge> getLabelEdges() {  // FIXME: why HashSet?
    return labelEdges;
  }

  public List<Exprent> getVarDefinitions() {
    return varDefinitions;
  }

  public List<Exprent> getExprents() {
    return exprents;
  }

  public void setExprents(List<Exprent> exprents) {
    this.exprents = exprents;
  }

  public boolean isCopied() {
    return copied;
  }

  public void setCopied(boolean copied) {
    this.copied = copied;
  }

  // helper methods
  public String toString() {
    return Integer.toString(id);
  }

  // *****************************************************************************
  // IMatchable implementation
  // *****************************************************************************

  @Override
  public IMatchable findObject(MatchNode matchNode, int index) {
    int node_type = matchNode.getType();

    if (node_type == MatchNode.MATCHNODE_STATEMENT && !this.stats.isEmpty()) {
      String position = (String)matchNode.getRuleValue(MatchProperties.STATEMENT_POSITION);
      if (position != null) {
        if (position.matches("-?\\d+")) {
          return this.stats.get((this.stats.size() + Integer.parseInt(position)) % this.stats.size()); // care for negative positions
        }
      }
      else if (index < this.stats.size()) { // use 'index' parameter
        return this.stats.get(index);
      }
    }
    else if (node_type == MatchNode.MATCHNODE_EXPRENT && this.exprents != null && !this.exprents.isEmpty()) {
      String position = (String)matchNode.getRuleValue(MatchProperties.EXPRENT_POSITION);
      if (position != null) {
        if (position.matches("-?\\d+")) {
          return this.exprents.get((this.exprents.size() + Integer.parseInt(position)) % this.exprents.size()); // care for negative positions
        }
      }
      else if (index < this.exprents.size()) { // use 'index' parameter
        return this.exprents.get(index);
      }
    }

    return null;
  }

  @Override
  public boolean match(MatchNode matchNode, MatchEngine engine) {
    if (matchNode.getType() != MatchNode.MATCHNODE_STATEMENT) {
      return false;
    }

    for (Entry<MatchProperties, MatchNode.RuleValue> rule : matchNode.getRules().entrySet()) {
      switch (rule.getKey()) {
        case STATEMENT_TYPE:
          if (this.type != rule.getValue().value) {
            return false;
          }
          break;
        case STATEMENT_STATSIZE:
          if (this.stats.size() != (Integer)rule.getValue().value) {
            return false;
          }
          break;
        case STATEMENT_EXPRSIZE:
          int exprsize = (Integer)rule.getValue().value;
          if (exprsize == -1) {
            if (this.exprents != null) {
              return false;
            }
          }
          else {
            if (this.exprents == null || this.exprents.size() != exprsize) {
              return false;
            }
          }
          break;
        case STATEMENT_RET:
          if (!engine.checkAndSetVariableValue((String)rule.getValue().value, this)) {
            return false;
          }
          break;
      }
    }

    return true;
  }

  public enum StatementType {
    GENERAL,
    IF,
    DO,
    SWITCH,
    TRY_CATCH,
    BASIC_BLOCK,
    // FINALLY,
    SYNCHRONIZED,
    PLACEHOLDER,
    CATCH_ALL,
    ROOT,
    DUMMY_EXIT,
    SEQUENCE
  }
}