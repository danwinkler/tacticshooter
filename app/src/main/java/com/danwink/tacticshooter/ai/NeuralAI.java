package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.phyloa.dlib.util.DMath;

public class NeuralAI extends ComputerPlayer {
	Net purchase;
	Net move;

	public NeuralAI() {
		super();
		purchase = new Net(11);
		purchase.addLayer(100);
		purchase.addLayer(100);
		purchase.addLayer(6);
		purchase.finalize();

		move = new Net(12);
		move.addLayer(100);
		move.addLayer(100);
		move.addLayer(4);
		move.finalize();
	}

	public void update(PathFinder finder) {
		/*
		 * Neural Nets:
		 * 
		 * 1. Purchasing
		 * Inputs:
		 * 5 inputs of number of friendly units (each type)
		 * 5 inputs of number of enemy units (each type)
		 * Money
		 * Outputs:
		 * 6 (each type + no buy)
		 * Highest output is chosen
		 * 
		 * 2. Per Unit Movement
		 * Inputs
		 * moving (0,1)
		 * on friendly point (0,1)
		 * point amount (0-1, 0 if moving == 1)
		 * distance to closest friendly point (on point doesn't count)
		 * number of friendly units on point
		 * number of enemy units on point
		 * number of friendly units moving to point
		 * distance to closest non-friendly point (on point doesn't count)
		 * number of friendly units on point
		 * number of enemy units on point
		 * number of friendly units moving to point
		 * distance to friendly point closest to enemy center
		 * number of friendly units on point
		 * number of enemy units on point
		 * number of friendly units moving to point
		 * 
		 * 
		 * Outputs
		 * don't move
		 * go to closest friendly point (on point doesn't count)
		 * go to closest non-friendly point (on point doesn't count)
		 * go to friendly point closest to enemy center
		 */

	}

	public class Net {
		ArrayList<Layer> layers = new ArrayList<Layer>();
		int inputs;

		public Net(int inputs) {
			this.inputs = inputs;
		}

		public void finalize() {
			for (int i = 0; i < layers.size(); i++) {
				if (i == 0) {
					layers.get(i).finalize(inputs);
				} else {
					layers.get(i).finalize(layers.get(i - 1).size());
				}
			}
		}

		public void seed() {
			layers.forEach(l -> seed());
		}

		public void addLayer(int size) {
			layers.add(new Layer(size));
		}
	}

	public class Layer {
		Neuron[] neurons;

		public Layer(int size) {
			neurons = new Neuron[size];
		}

		public int size() {
			return neurons.length;
		}

		public void finalize(int inputSize) {
			for (Neuron n : neurons) {
				n.finalize(inputSize);
			}
		}

		public void seed() {
			for (Neuron n : neurons) {
				n.seed();
			}
		}
	}

	public class Neuron {
		float[] inputWeights;
		float bias;

		public Neuron() {

		}

		public void finalize(int inputSize) {
			inputWeights = new float[inputSize];
		}

		public void seed() {
			for (int i = 0; i < inputWeights.length; i++) {
				inputWeights[i] = DMath.randomf(-1, 1);
			}
			bias = DMath.randomf(-1, 1);
		}
	}
}
