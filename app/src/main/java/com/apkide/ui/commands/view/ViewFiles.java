package com.apkide.ui.commands.view;

import androidx.annotation.NonNull;

import com.apkide.ui.App;
import com.apkide.ui.MenuCommand;
import com.apkide.ui.R;

public class ViewFiles implements MenuCommand {
    @Override
    public int getId() {
        return R.id.mainActionViewFiles;
    }

    @NonNull
    @Override
    public String getName() {
        return "Files";
    }

    @Override
    public boolean commandPerformed() {
        App.getMainUI().toggleFileBrowser();
        return true;
    }
}
