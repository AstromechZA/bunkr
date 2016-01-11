package org.bunkr.gui.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.gui.components.treeview.CellFactoryCallback;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;

/**
 * Creator: benmeier
 * Created At: 2015-12-28
 */
public class ContextMenus
{
    private static final String STR_NEW_FILE = "New File";
    private static final String STR_NEW_FOLDER = "New Folder";
    private static final String STR_DELETE = "Delete";
    private static final String STR_RENAME = "Rename";
    private static final String STR_INFO = "Info";
    private static final String STR_IMPORT_FILE = "Import File";
    private static final String STR_OPEN = "Open";
    private static final String STR_EXPORT = "Export File";

    public final ContextMenu dirContextMenu, fileContextMenu, rootContextMenu;
    public final MenuItem rootNewFile, rootNewSubDir, rootImportFile,
            dirNewFile, dirNewSubDir, dirImportFile, dirDelete, dirRename,
            fileDelete, fileRename, fileInfo, fileOpen, fileExport;

    private final InventoryTreeView treeView;

    public ContextMenus(InventoryTreeView treeView)
    {
        this.treeView = treeView;

        // root
        this.rootNewFile = new MenuItem(STR_NEW_FILE);
        this.rootNewSubDir = new MenuItem(STR_NEW_FOLDER);
        this.rootImportFile = new MenuItem(STR_IMPORT_FILE);

        // file
        this.fileDelete = new MenuItem(STR_DELETE);
        this.fileRename = new MenuItem(STR_RENAME);
        this.fileInfo = new MenuItem(STR_INFO);
        this.fileOpen = new MenuItem(STR_OPEN);
        this.fileExport = new MenuItem(STR_EXPORT);

        // dir
        this.dirDelete = new MenuItem(STR_DELETE);
        this.dirRename = new MenuItem(STR_RENAME);
        this.dirNewFile = new MenuItem(STR_NEW_FILE);
        this.dirNewSubDir = new MenuItem(STR_NEW_FOLDER);
        this.dirImportFile = new MenuItem(STR_IMPORT_FILE);

        this.dirContextMenu = new ContextMenu(this.dirNewFile, this.dirNewSubDir, this.dirRename, this.dirDelete, this.dirImportFile);
        this.fileContextMenu = new ContextMenu(this.fileOpen, this.fileInfo, this.fileExport, this.fileRename, this.fileDelete);
        this.rootContextMenu = new ContextMenu(this.rootNewFile, this.rootNewSubDir, this.rootImportFile);

        this.treeView.setCellFactory(new CellFactoryCallback(this));

        this.treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
            {
                try
                {
                    TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItemOrNull();
                    if (selected != null && selected.getValue().getType() == InventoryTreeData.Type.FILE)
                        this.fileOpen.getOnAction().handle(new ActionEvent());
                }
                catch (BaseBunkrException e)
                {
                    QuickDialogs.exception(e);
                }
            }
        });
    }
}
