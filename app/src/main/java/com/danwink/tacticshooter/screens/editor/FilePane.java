package com.danwink.tacticshooter.screens.editor;

import java.awt.Color;
import java.io.File;

import org.dom4j.DocumentException;
import org.newdawn.slick.Image;

import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.screens.editor.EditorScreen.LevelElement;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.renderer.Renderer2D;

public class FilePane extends DColumnPanel {
    public FilePane(LevelElement levelElement, int uiScale, DUI dui) {
        super();
        // New Pane - shows up when you click new
        var newPane = new DColumnPanel();
        newPane.setRelativePosition(RelativePosition.CENTER, 0, 0);
        newPane.setDrawBackground(true);
        newPane.addSpacer(10 * uiScale);
        var widthBox = new LabeledTextBox("Width:", 75 * uiScale, 75 * uiScale, 50 * uiScale);
        widthBox.setText("63");
        newPane.add(widthBox);
        newPane.addSpacer(10 * uiScale);
        var heightBox = new LabeledTextBox("Height:", 75 * uiScale, 75 * uiScale, 50 * uiScale);
        heightBox.setText("63");
        newPane.add(heightBox);
        newPane.addSpacer(10 * uiScale);

        var buttonPane = new DRowPanel(0, 0, 0, 0);
        var ok = new DButton("Ok", 0, 0, 75 * uiScale, 50 * uiScale);
        buttonPane.add(ok);
        var cancel = new DButton("Cancel", 0, 0, 75 * uiScale, 50 * uiScale);
        buttonPane.add(cancel);
        ok.onMouseUp(e -> {
            try {
                int width = Integer.parseInt(widthBox.getText());
                int height = Integer.parseInt(heightBox.getText());
                levelElement.newLevel(width, height);
                ui.hideTopPanel();
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number");
            }
        });
        cancel.onMouseUp(e -> {
            dui.hideTopPanel();
        });
        newPane.add(buttonPane);
        newPane.addSpacer(10 * uiScale);

        // Open Pane - shows up when you click open
        var openPane = new DColumnPanel();
        openPane.setRelativePosition(RelativePosition.CENTER, 0, 0);
        openPane.setDrawBackground(true);
        openPane.addSpacer(10 * uiScale);
        var fileExplorer = new FileExplorer(500 * uiScale, 400 * uiScale, 20 * uiScale);
        fileExplorer.setView(new File("levels"));
        openPane.add(fileExplorer);
        openPane.addSpacer(10 * uiScale);
        var openPaneButtonRow = new DRowPanel();
        var openPaneOpenButton = new DButton("Open", 0, 0, 75 * uiScale, 50 * uiScale);
        openPaneOpenButton.onMouseUp(e -> {
            File f = fileExplorer.getSelectedFile();
            if (f != null && f.isFile()) {
                try {
                    levelElement.setLevel(LevelFileHelper.loadLevel(f));
                } catch (DocumentException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                ui.hideTopPanel();
            }
        });
        openPaneButtonRow.add(openPaneOpenButton);
        var openPaneCancelButton = new DButton("Cancel", 0, 0, 75 * uiScale, 50 * uiScale);
        openPaneCancelButton.onMouseUp(e -> {
            ui.hideTopPanel();
        });
        openPaneButtonRow.add(openPaneCancelButton);
        openPane.add(openPaneButtonRow);

        var newButton = new DButton("New", 0, 0, 150 * uiScale, 50 * uiScale);
        newButton.onMouseUp(e -> {
            ui.setTopPanel(this, newPane);
        });
        add(newButton);

        var openButton = new DButton("Open", 0, 0, 150 * uiScale, 50 * uiScale);
        openButton.onMouseUp(e -> {
            ui.setTopPanel(this, openPane);
        });
        add(openButton);
    }

    public class LabeledTextBox extends DRowPanel {
        DTextBox textBox;

        public LabeledTextBox(String label, int textWidth, int boxWidth, int height) {
            super();
            var labelBox = new DText(label, true);
            labelBox.setSize(textWidth, height);

            add(labelBox);
            textBox = new DTextBox(0, 0, boxWidth, height);
            add(textBox);
        }

        public String getText() {
            return textBox.getText();
        }

        public void setText(String text) {
            textBox.setText(text);
        }
    }

    public class FileExplorer extends DPanel {
        DScrollPane scrollPane;
        FileExplorerList list;
        int lineHeight;

        public FileExplorer(int width, int height, int lineHeight) {
            super(0, 0, width, height);
            this.lineHeight = lineHeight;

            setDrawBackground(true);

            list = new FileExplorerList();
            scrollPane = new DScrollPane(0, 0, width, height);
            scrollPane.add(list);

            add(scrollPane);
        }

        public void setView(File file) {
            list.setView(file);
        }

        public File getSelectedFile() {
            return list.getSelectedFile();
        }

        public class FileExplorerList extends DUIElement {
            Color hoverColor = new Color(180, 180, 255);
            Color textColor = new Color(0, 0, 0);

            File[] files;
            int selectedIndex = -1;
            long lastClick = 0;
            int doubleClickInterval = 500;

            @Override
            public void render(Renderer2D<Image> r) {
                r.color(textColor);
                if (selectedIndex == 0) {
                    r.color(hoverColor);
                    r.fillRect(0, 0, width, lineHeight);
                    r.color(textColor);
                }
                r.text("..", 0, 0);
                for (int i = 0; i < files.length; i++) {
                    if (i + 1 == selectedIndex) {
                        r.color(hoverColor);
                        r.fillRect(0, (i + 1) * lineHeight, width - 20, lineHeight);
                        r.color(textColor);
                    }
                    r.text(files[i].getName(), 0, (i + 1) * lineHeight);
                }
            }

            @Override
            public void update(DUI ui) {
                // TODO Auto-generated method stub

            }

            public void setView(File file) {
                files = file.listFiles();

                height = (files.length + 1) * lineHeight;
                width = FileExplorer.super.width;

                doLayout(ui);
                scrollPane.setInnerPaneHeight(this.height);
            }

            public File getSelectedFile() {
                if (selectedIndex == 0) {
                    return files[0].getParentFile();
                } else if (selectedIndex > 0) {
                    return files[selectedIndex - 1];
                } else {
                    return null;
                }
            }

            @Override
            public void keyPressed(DKeyEvent dke) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(DKeyEvent dke) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(DMouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(DMouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean mousePressed(DMouseEvent e) {
                int index = e.y / lineHeight;
                if (index == selectedIndex) {
                    if (System.currentTimeMillis() - lastClick < doubleClickInterval) {
                        if (selectedIndex == 0) {
                            setView(files[0].getAbsoluteFile().getParentFile().getParentFile());
                        } else {
                            File file = files[selectedIndex - 1];
                            if (file.isDirectory()) {
                                setView(file);
                            }
                        }
                    }
                }
                selectedIndex = index;
                lastClick = System.currentTimeMillis();
                return false;
            }

            @Override
            public boolean mouseReleased(DMouseEvent e) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void mouseMoved(DMouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean mouseDragged(DMouseEvent e) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void mouseWheel(DMouseEvent dme) {
                scrollPane.mouseWheel(dme);
            }
        }
    }
}
