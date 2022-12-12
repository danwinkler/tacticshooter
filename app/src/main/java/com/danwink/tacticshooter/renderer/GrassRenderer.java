package com.danwink.tacticshooter.renderer;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.phyloa.dlib.math.Point2i;

/*
 * The current implementation of this is way too inefficient, but it's just for fun
 */
public class GrassRenderer {
    Image texture;
    ArrayList<Point2i> growthPoints = new ArrayList<Point2i>();
    int[][] grassSize;
    float wind;

    public void update(ClientState cs) {
        if (cs.l == null || grassSize == null) {
            return;
        }

        wind += 0.01f;
        for (int x = 0; x < cs.l.width; x++) {
            for (int y = 0; y < cs.l.height; y++) {
                if (cs.l.getTile(x, y) == TileType.FLOOR) {
                    int walkTime = cs.lastWalked[x][y] - cs.frame;
                    if (walkTime < 2) {
                        grassSize[x][y] = Math.max(grassSize[x][y] - 10, 0);
                    } else if (walkTime > 100) {
                        grassSize[x][y] = Math.min(grassSize[x][y] + 1, 1000);
                    }
                }
            }
        }
    }

    public void render(Graphics g, ClientState cs) {
        if (texture == null) {
            if (cs.l != null) {
                generateTexture(cs);
                grassSize = new int[cs.l.width][cs.l.height];
                findGrowthPoints(cs);
            } else {
                return;
            }
        }

        g.setColor(Color.green);
        float windX = 1 + (float) Math.cos(wind) * .2f;
        float windY = 1 + (float) Math.sin(wind) * .05f;
        g.setLineWidth(3);
        for (int x = 0; x < cs.l.width; x++) {
            for (int y = 0; y < cs.l.height; y++) {
                if (cs.l.getTile(x, y) == TileType.FLOOR) {
                    int size = grassSize[x][y];
                    for (var p : growthPoints) {
                        var dx = (size / 100f * 3) * windX;
                        var dy = (size / 100f * 12) * windY;
                        g.drawLine(
                                x * cs.l.tileSize + p.x,
                                y * cs.l.tileSize + p.y,
                                x * cs.l.tileSize + p.x + dx,
                                y * cs.l.tileSize + p.y + dy);
                    }
                }
            }
        }
        g.setLineWidth(1);

        g.drawImage(texture, 0, 0);
    }

    private void findGrowthPoints(ClientState cs) {
        growthPoints.clear();

        var floorTexture = cs.l.theme.floor;
        var tileSize = floorTexture.getWidth() / 3;
        var floorRegion = floorTexture.getSubImage(tileSize, tileSize * 2, tileSize, tileSize);

        int minDist = 6;
        // find dark spots in the image, that aren't too close to other spots
        for (int x = 0; x < floorRegion.getWidth(); x++) {
            for (int y = 0; y < floorRegion.getHeight(); y++) {
                var c = floorRegion.getColor(x, y);
                if (isDark(c)) {
                    boolean tooClose = false;
                    for (var p : growthPoints) {
                        if (Math.abs(p.x - x) < minDist && Math.abs(p.y - y) < minDist) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) {
                        growthPoints.add(new Point2i(x, y));
                    }
                }
            }
        }
    }

    public boolean isDark(Color c) {
        float maxBright = 0.5f;
        return c.r < maxBright && c.g < maxBright && c.b < maxBright;
    }

    private void generateTexture(ClientState cs) {
        try {
            texture = new Image(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
