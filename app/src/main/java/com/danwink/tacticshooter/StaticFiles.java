package com.danwink.tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import com.phyloa.dlib.renderer.DScreenTransition;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

@SuppressWarnings("deprecation")
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

	static {
		try {
			names = DFile.loadText("data" + File.separator + "dist.male.first.txt");
		} catch (FileNotFoundException e) {
			System.err.println("Make sure the names file is located at: data" + File.separator + "dist.male.first.txt");
		}
	}

	public static void loadAllMusic() {
		if (!ready && !started) {
			new Thread(new Runnable() {
				public void run() {
					started = true;
					loadMusic("menu", "Deliberate Thought.ogg");
					ready = true; // We can go to menu when menu music is done
					loadMusic("play1", "Decisions.ogg");
					loadMusic("play2", "Finding the Balance.ogg");
					loadMusic("play3", "Rising.ogg");

					loadSound("bullet1", "bullet1.wav");
					loadSound("bullet2", "bullet2.wav");
					loadSound("ping1", "ping1.wav");
					loadSound("death1", "death1.wav");
					loadSound("death2", "death2.wav");
					loadSound("hit1", "hit1.wav");
					loadSound("explode1", "explode1.wav");
				}
			}).start();
		}
	}

	static void loadMusic(String name, String file) {
		try {
			status = "Loading " + file;
			music.put(name, new Music("data" + File.separator + "sound" + File.separator + file));
		} catch (SlickException e) {

		}
	}

	static void loadSound(String name, String file) {
		try {
			status = "Loading " + file;
			sound.put(name, new Sound("data" + File.separator + "sound" + File.separator + file));
		} catch (SlickException e) {

		}
	}

	static void loadImage(String name, String file) {

	}

	public static void loopMusic(String name) {
		if (ready) {
			getMusic(name).loop();
		} else {
			loopWhenReady(name);
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

	public static Music getMusic(String name) {
		return music.get(name);
	}

	public static Sound getSound(String name) {
		return sound.get(name);
	}

	public static DScreenTransition<GameContainer, Graphics> getDownMenuOut() {
		return new DScreenSlideTransition(-1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), false);
	}

	public static DScreenTransition<GameContainer, Graphics> getDownMenuIn() {
		return new DScreenSlideTransition(-1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), true);
	}

	public static DScreenTransition<GameContainer, Graphics> getUpMenuOut() {
		return new DScreenSlideTransition(1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), false);
	}

	public static DScreenTransition<GameContainer, Graphics> getUpMenuIn() {
		return new DScreenSlideTransition(1, 0, StaticFiles.advOptions.getF("menuTransitionSpeed"), true);
	}

	public static String getUsername() {
		return options.getS("playerName");
	}
}
