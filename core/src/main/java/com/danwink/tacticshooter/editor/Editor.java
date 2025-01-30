package com.danwink.tacticshooter.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.danwink.tacticshooter.KryoHelper;
import com.danwink.tacticshooter.editor.BrushPanel.Brush;
import com.danwink.tacticshooter.editor.ModePanel.EditMode;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Editor {
	public static final int DEFAULT_WIDTH = 1024;
	public static final int DEFAULT_HEIGHT = 768;

	public JFrame container;
	public JMenuBar menubar;
	public JTabbedPane tabs;

	public JPanel mapEditTab;
	public OptionsPanel optionsPanel;
	public CodePanel codePanel;
	public CodePanel umsPanel;

	public BrushPanel brushPanel;
	public BuildingPanel buildingPanel;

	public MapPane mapPane;
	public ModePanel modePanel;

	public JScrollPane scrollPane;

	NewDialog newDialog;
	ResizeDialog resizeDialog;
	BuildingDialog buildingDialog;

	Level l;
	Building selected;

	LinkedList<byte[]> undoStack = new LinkedList<byte[]>();

	Kryo kryo;

	boolean mapChangedDuringUndoableChange = false;

	public Editor() {
		kryo = new Kryo();
		KryoHelper.register(kryo);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		container = new JFrame("TacticShooter Editor");
		container.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		container.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		menubar = new JMenuBar();
		container.setJMenuBar(menubar);

		menubar.add(new FileMenu(this));
		menubar.add(new EditMenu(this));

		tabs = new JTabbedPane();

		mapEditTab = new JPanel();
		mapEditTab.setLayout(new BorderLayout());

		modePanel = new ModePanel(this);
		mapEditTab.add(modePanel, BorderLayout.NORTH);

		mapPane = new MapPane(this);

		scrollPane = new JScrollPane(mapPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapEditTab.add(scrollPane, BorderLayout.CENTER);

		optionsPanel = new OptionsPanel(this);
		codePanel = new CodePanel(this);
		umsPanel = new CodePanel(this);

		tabs.addTab("Map", mapEditTab);
		tabs.addTab("Options", optionsPanel);
		tabs.addTab("Code", codePanel);
		tabs.addTab("ums", umsPanel);

		container.add(tabs);

		container.pack();
		container.setVisible(true);

		// newLevel( 20, 20 );
	}

	public void showNewDialog() {
		newDialog = new NewDialog(this);
	}

	public void newLevel(int width, int height) {
		loadLevel(new Level(width, height));
	}

	public void loadLevel(Level l) {
		this.l = l;
		mapPane.updateLevel();
		codePanel.updateCode(l.code);
		umsPanel.updateCode(l.ums);
		optionsPanel.updateOptions(l);
		undoStack.clear();
		pushUndoState();

		setMode(EditMode.TILE);
	}

	public void setMode(EditMode mode) {
		if (brushPanel != null) {
			mapEditTab.remove(brushPanel);
			brushPanel = null;
		}

		if (buildingPanel != null) {
			mapEditTab.remove(buildingPanel);
			buildingPanel = null;
		}

		switch (mode) {
			case BUILDING:
				buildingPanel = new BuildingPanel(this);
				mapEditTab.add(buildingPanel, BorderLayout.WEST);
				break;
			case CODE:
				break;
			case TILE:
				brushPanel = new BrushPanel(this);
				mapEditTab.add(brushPanel, BorderLayout.WEST);
				break;
			default:
				break;
		}

		mapEditTab.validate();
		// container.pack();
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Editor e = new Editor();
	}

	public Brush getBrush() {
		return brushPanel.getBrush();
	}

	public void redrawLevel() {
		mapPane.redrawLevel();
	}

	public void toggleOptions() {

	}

	public void toggleCode() {

	}

	public void interact(int x, int y) {
		switch (modePanel.mode) {
			case BUILDING:
				for (Building b : l.buildings) {
					if (b.x == x * Level.tileSize + Level.tileSize / 2
							&& b.y == y * Level.tileSize + Level.tileSize / 2) {
						buildingDialog = new BuildingDialog(this, b);
						break;
					}
				}
				break;
			case CODE:
				break;
			case TILE:
				break;
			default:
				break;

		}
	}

	public void place(int x, int y) {
		if (x < 0 || x >= l.width || y < 0 || y >= l.height)
			return;
		switch (modePanel.mode) {
			case TILE:
				switch (brushPanel.brush) {
					case FLOOR:
						setTile(x, y, TileType.FLOOR);
						break;
					case WALL:
						setTile(x, y, TileType.WALL);
						break;
					case DOOR:
						setTile(x, y, TileType.DOOR);
						break;
					case GRATE:
						setTile(x, y, TileType.GRATE);
						break;
					default:
						break;
				}
				break;
			case BUILDING:
				Building b = l.getBuilding(x, y);
				if (b == null) {
					l.buildings.add(new Building(x * Level.tileSize + Level.tileSize / 2,
							y * Level.tileSize + Level.tileSize / 2, buildingPanel.type, null));
				} else {
					selected = b;
				}
				break;
			case CODE:
				break;
			default:
				break;
		}

		redrawLevel();
		container.repaint();
	}

	public static TileType[][] copy(TileType[][] arr) {
		TileType[][] copy = new TileType[arr[0].length][arr.length];
		for (int y = 0; y < arr.length; y++)
			for (int x = 0; x < arr[y].length; x++)
				copy[x][y] = arr[x][y];
		return copy;
	}

	public void setTile(int x, int y, TileType t) {
		internalSetTile(x, y, t);
		switch (brushPanel.mirrorMode) {
			case "None":
				break;
			case "X":
				internalSetTile((l.width - 1) - x, y, t);
				break;
			case "Y":
				internalSetTile(x, (l.height - 1) - y, t);
				break;
			case "XY":
				internalSetTile((l.width - 1) - x, (l.height - 1) - y, t);
				break;
			case "4":
				internalSetTile((l.width - 1) - x, y, t);
				internalSetTile(x, (l.height - 1) - y, t);
				internalSetTile((l.width - 1) - x, (l.height - 1) - y, t);
				break;
		}
	}

	public void internalSetTile(int x, int y, TileType t) {
		if (l.tiles[x][y] == t)
			return;
		mapChangedDuringUndoableChange = true;
		switch (brushPanel.drawType) {
			case FILL:
				TileType old = l.tiles[x][y];
				l.tiles[x][y] = t;
				if (x > 0 && l.tiles[x - 1][y] == old)
					internalSetTile(x - 1, y, t);
				if (x < l.width - 1 && l.tiles[x + 1][y] == old)
					internalSetTile(x + 1, y, t);
				if (y > 0 && l.tiles[x][y - 1] == old)
					internalSetTile(x, y - 1, t);
				if (y < l.height - 1 && l.tiles[x][y + 1] == old)
					internalSetTile(x, y + 1, t);
				break;
			case PENCIL:
				l.tiles[x][y] = t;
				break;
			default:
				break;
		}
	}

	public void beginUndoableChange() {
		System.out.println("beginUndoableChange");
		mapChangedDuringUndoableChange = false;
	}

	public void endUndoableChange() {
		System.out.println("endUndoableChange");
		if (mapChangedDuringUndoableChange) {
			pushUndoState();
			mapChangedDuringUndoableChange = false;
		}
	}

	public void pushUndoState() {
		System.out.println("pushUndoState");
		Output output = new Output(1024, -1);
		kryo.writeObject(output, l);
		undoStack.push(output.toBytes());
	}

	public void undo() {
		if (undoStack.size() > 1) {
			undoStack.pop();
			System.out.println("undo");
			Input input = new Input(undoStack.peek());
			l = kryo.readObject(input, Level.class);
			mapPane.updateLevel();
			container.repaint();
		}
	}

	public void delete() {
		if (selected != null) {
			l.buildings.remove(selected);
			selected = null;
			redrawLevel();
			container.repaint();
			pushUndoState();
		}
	}

	public void updateLevel() {
		l.code = codePanel.textArea.getText();
		l.ums = umsPanel.textArea.getText();
	}

	public void showResizeDialog() {
		resizeDialog = new ResizeDialog(this);
	}
}
