package com.danwink.tacticshooter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.audio.Music;

public class MusicQueuer {
	static List<Music> currentQueue;
	static int currentIndex = 0;
	static boolean currentlyPlaying = false;

	public static void loopTracks(String... trackNames) {
		if (currentlyPlaying) {
			currentQueue.get(currentIndex).stop();
		}

		currentQueue = Stream.of(trackNames).map(Assets::getMusic).collect(Collectors.toList());
		currentIndex = 0;

		startTrack();
	}

	public static void shuffleTracks(String... trackNames) {
		Collections.shuffle(Arrays.asList(trackNames));
		loopTracks(trackNames);
	}

	static void startTrack() {
		var music = currentQueue.get(currentIndex);
		currentlyPlaying = true;
		music.play();
		music.setOnCompletionListener(m -> {
			currentIndex++;
			if (currentIndex >= currentQueue.size()) {
				currentIndex = 0;
			}
			startTrack();
		});
	}
}
