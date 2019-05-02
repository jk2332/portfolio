//
//  Board.hpp
//  WeatherDefender (Mac)
//
//  Created by 김지원 on 3/8/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Board_hpp
#define Board_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>
#include "Constants.hpp"


using namespace cugl;

#pragma mark -
#pragma mark Board
/**
 * A ragdoll whose body parts are boxes connected by joints
 *
 * Note that this module handles its own scene graph management.  Since a
 * ComplexObstacle owns all of its child obstacles, it is natural for it to
 * own the corresponding as well scene graph.
 *
 * For the construction, see the ragdoll diagram above, with the position offsets.
 */
class Board {
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Board);
    
protected:
    float _drawscale;
    std::shared_ptr<cugl::Texture> _texture;
    int _gridNumX;
    float _screenWidth;
    float _screenHeight;
    int _gridNumY;
    std::vector<std::shared_ptr<cugl::Node>> _nodes;
//    float _offsetX;
//    float offsetY = 0.3;
//    float _offsetX = 0.65;

public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Board.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Board(void) { }
    
    /**
     * Destroys this Board, releasing all resources.
     */
    virtual ~Board(void) { dispose(); }
    
    /**
     * Disposes all resources and assets of this Board
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a Ragdoll may not be used until it is initialized again.
     */
    void dispose();
    
    /**
     * Initializes a new Board with the given position and scale
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return  true if the obstacle is initialized properly, false otherwise.
     */
    bool init(float screenWidth, float scale, std::shared_ptr<cugl::Texture> texture, int gridNumX, int gridNumY);
    
    
#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated Board
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return a newly allocated Board
     */
    static std::shared_ptr<Board> alloc(float screenWidth, float scale, std::shared_ptr<Texture> texture, int gridNumX, int gridNumY) {
        std::shared_ptr<Board> result = std::make_shared<Board>();
        return (result->init(screenWidth, scale, texture, gridNumX, gridNumY) ? result : nullptr);
    }
 
#pragma mark -
#pragma mark Accessors
    int getPlantStatus();
    void setPlantStatus(int s);
    
    bool isGoalTile();
    bool wasVisited();
    void tileReset();
    
#pragma mark -
#pragma mark Animation
    
#pragma mark -
#pragma mark Scene Graph Management
    /**
     * Sets the scene graph node representing this Board.
     *
     * Note that this method also handles creating the nodes for the body parts
     * of this Ragdoll. Since the obstacles are decoupled from the scene graph,
     * initialization (which creates the obstacles) occurs prior to the call to
     * this method. Therefore, to be drawn to the screen, the nodes of the attached
     * bodies must be added here.
     *
     * The bubbler also uses the world node when adding bubbles to the scene, so
     * the input node must be added to the world BEFORE this method is called.
     *
     * By storing a reference to the scene graph node, the model can update
     * the node to be in sync with the physics info. It does this via the
     * {@link Obstacle#update(float)} method.
     *
     * @param node  The scene graph node representing this Board, which has been added to the world node already.
     */
    void setSceneNode(const std::shared_ptr<cugl::Node>& node);
    
    Vec2 gridCoordToPosition(cugl::Vec2 p){
        return gridCoordToPosition(p.x, p.y);
    }
    
    Vec2 gridCoordToPosition(float x, float y){
        Vec2 a = _drawscale*Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH * x + GRID_WIDTH/2 + GRID_OFFSET_X * x),
                            (DOWN_LEFT_CORNER_Y + GRID_HEIGHT * y - GRID_HEIGHT/2 + GRID_OFFSET_Y * y));
        return a;
    }
    
    std::pair<int, int> posToGridCoord(cugl::Vec2 p){
        return posToGridCoord(p.x, p.y);
    }
    
    std::pair<int, int> posToGridCoord(float x, float y){
        if (!isInBounds(x, y)){
            return std::make_pair(-1, -1);
        }
        else {
            int new_x = int((x - DOWN_LEFT_CORNER_X)/(GRID_WIDTH + GRID_OFFSET_X));
            int new_y = int((y - DOWN_LEFT_CORNER_Y)/(GRID_HEIGHT +GRID_OFFSET_Y));
            return std::make_pair(new_x, new_y);
        }
    }
    
    bool isInBounds(cugl::Vec2 p){
        return isInBounds(p.x, p.y);
    }
    
    bool isInBounds(float x, float y){
        if (x <= DOWN_LEFT_CORNER_X || x >= DOWN_LEFT_CORNER_X + GRID_WIDTH*_gridNumX + GRID_OFFSET_X * x || y <= DOWN_LEFT_CORNER_Y || y >= DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_gridNumY + GRID_OFFSET_Y * y){
            return false;
        }
        return true;
    }
    
    std::shared_ptr<cugl::Node> getNodeAt(int x, int y);
    
    /**
     * Sets the ratio of the Ragdoll sprite to the physics body
     *
     * The Ragdoll needs this value to convert correctly between the physics
     * coordinates and the drawing screen coordinates.  Otherwise it will
     * interpret one Box2D unit as one pixel.
     *
     * All physics scaling must be uniform.  Rotation does weird things when
     * attempting to scale physics by a non-uniform factor.
     *
     * @param scale The ratio of the Ragdoll sprite to the physics body
     */
    void setDrawScale(float scale);
};

#endif
