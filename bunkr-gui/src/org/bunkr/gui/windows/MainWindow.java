package org.bunkr.gui.windows;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.Logging;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.FilesTabPaneController;
import org.bunkr.gui.controllers.InventoryCMController;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-26
 */
public class MainWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;

    private final ArchiveInfoContext archive;
    private final String cssPath;
    private final UserSecurityProvider securityProvider;

    private Label lblHierarchy;
    private TabPane tabPane;
    private InventoryTreeView tree;

    public MainWindow(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super();
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.cssPath = Resources.getExternalPath("/resources/css/main_window.css");
        this.initialise();

        FilesTabPaneController tabPaneController = new FilesTabPaneController(this.archive, this.tabPane);
        tabPaneController.setOnSaveInventoryRequest(s -> this.saveMetadata());

        InventoryCMController contextMenuController = new InventoryCMController(this.archive, this.tree);
        contextMenuController.bindEvents();
        contextMenuController.setOnSaveInventoryRequest(s -> this.saveMetadata());
        contextMenuController.setOnRenameFile(tabPaneController::notifyRename);
        contextMenuController.setOnDeleteFile(f -> tabPaneController.requestClose(f, false));
        contextMenuController.setOnOpenFile(tabPaneController::requestOpen);

        this.tree.refreshAll();
    }

    @Override
    public void initControls()
    {
        this.lblHierarchy = new Label("File Structure");
        this.tree = new InventoryTreeView(this.archive);
        this.tabPane = new TabPane();
    }

    @Override
    public Parent initLayout()
    {
        SplitPane sp = new SplitPane();
        sp.setDividerPosition(0, 0.3);

        VBox leftBox = new VBox(10);
        leftBox.getChildren().add(this.lblHierarchy);
        VBox.setVgrow(this.lblHierarchy, Priority.NEVER);
        leftBox.getChildren().add(this.tree);
        VBox.setVgrow(this.tree, Priority.ALWAYS);

        sp.getItems().add(leftBox);
        sp.getItems().add(this.tabPane);
        return sp;
    }

    @Override
    public void bindEvents()
    {

    }

    @Override
    public void applyStyling()
    {
        this.lblHierarchy.setAlignment(Pos.CENTER);
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle(String.format("Bunkr - %s", this.archive.filePath.getAbsolutePath()));
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }

    private void saveMetadata()
    {
        try
        {
            Logging.info("Saving Archive Metadata");
            MetadataWriter.write(this.archive, this.securityProvider);
            Logging.info("Saved Archive Metadata");
        }
        catch (IOException | BaseBunkrException e)
        {
            QuickDialogs.exception(e);
        }
    }
}
