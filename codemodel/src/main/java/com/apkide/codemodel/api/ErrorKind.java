package com.apkide.codemodel.api;

public interface ErrorKind {
   int PARSER_ERROR = 0;
   int SYNTAX_ERROR = 1;
   int RULE_ERROR = 2;
   int FLOW_ERROR = 3;
   int SEMANTIC_ERROR = 4;
   int SEMANTIC_WARNING = 5;
   int UNCHECKED_WARNING = 6;
   int FALLTHROUGH_WARNING = 8;
   int UNUSEDVARIABLE_WARNING = 9;
   int UNUSEDPARAMETER_WARNING = 10;
   int UNUSEDLOCALCODE_WARNING = 11;
   int REDUNDANTCAST_WARNING = 12;
   int EMPTYSTATEMENT_WARNING = 13;
   int IDENTICALIFELSE_WARNING = 14;
   int STATICACCESS_WARNING = 15;
   int REDUNDANTASSIGNMENT_WARNING = 16;
   int REDUNDANTTHROWS_WARNING = 23;
   int UPCAST_WARNING = 24;
   int DEPRECATED_WARNING = 25;
   int JAVA15_WARNING = 26;
   int TODO = 17;
   int UNREACHEDCLASS_INSPECTION = 18;
   int UNREACHEDCODE_INSPECTION = 19;
   int CANBEPRIVATECLASS_INSPECTION = 20;
   int CANBEPRIVATECODE_INSPECTION = 21;
   int RETURN_INSPECTION = 22;
   int OVERRIDDEN_METHOD = 30;
   int OVERRIDING_METHOD = 31;
}
