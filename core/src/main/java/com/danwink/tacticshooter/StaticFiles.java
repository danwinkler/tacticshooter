package com.danwink.tacticshooter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import org.newdawn.slick.Music;
import org.newdawn.slick.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.danwink.tacticshooter.dal.DAL;
import com.phyloa.dlib.renderer.DScreenTransition;
import com.phyloa.dlib.util.DOptions;

public class StaticFiles {
	static HashMap<String, Music> music = new HashMap<String, Music>();
	static HashMap<String, Sound> sound = new HashMap<String, Sound>();

	public static DOptions options = new DOptions("options.txt");
	public static DOptions advOptions = new DOptions("data" + File.separator + "advoptions.txt");

	public static BackgroundDrawer bgd = new BackgroundDrawer();

	public static boolean ready = false;

	private static boolean started = false;
	public static String names;

	public static String status = "";

	public static File gameModeDir = new File("data" + File.separator + "gamemodes");

	public static BitmapFont[] fonts = new BitmapFont[3];

	static {
		names = Gdx.files.internal("data" + File.separator + "dist.male.first.txt").readString();
	}

	public static void loadAll() {
		if (!ready && !started) {
			fonts[0] = new BitmapFont(Gdx.files.internal("data" + File.separator + "pixelfont1_16px.fnt"),
					Gdx.files.internal("data" + File.separator + "pixelfont1_16px.png"), true);
			fonts[1] = new BitmapFont(Gdx.files.internal("data" + File.separator + "pixelfont1_32px.fnt"),
					Gdx.files.internal("data" + File.separator + "pixelfont1_32px.png"), true);
			fonts[2] = new BitmapFont(Gdx.files.internal("data" + File.separator + "pixelfont1_48px.fnt"),
					Gdx.files.internal("data" + File.separator + "pixelfont1_48px.png"), true);
		}
	}

	public static void loopWhenReady(final String name) {
		new Thread(new Runnable() {
			public void run() {
				while (!ready) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Music m = music.get(name);
				if (!m.playing()) {
					m.loop();
				}
			}
		}).start();
	}

	public static DScreenTransition<DAL> getDownMenuOut() {
		return new DScreenSlideTransition(-1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), false);
	}

	public static DScreenTransition<DAL> getDownMenuIn() {
		return new DScreenSlideTransition(-1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), true);
	}

	public static DScreenTransition<DAL> getUpMenuOut() {
		return new DScreenSlideTransition(1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), false);
	}

	public static DScreenTransition<DAL> getUpMenuIn() {
		return new DScreenSlideTransition(1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), true);
	}

	public static String getUsername() {
		return options.getS("playerName");
	}

	public static String[] getGameTypes() {
		// Order is: pointcapture, then everything else, then UMS
		return Stream.concat(
				Stream.concat(Stream.of("pointcapture"), Arrays.stream(gameModeDir.listFiles()).filter(f -> f.isFile())
						.filter(f -> {
							// Remove pointcapture (we'll add it back in as the first choice)
							return !f.getName().contains("pointcapture");
						}).map(f -> {
							// Remove suffix
							String name = f.getName();
							int i = name.lastIndexOf('.');
							if (i > 0) {
								name = name.substring(0, i);
							}
							return name;
						})),
				Stream.of("UMS")).toArray(String[]::new);
	}
}
