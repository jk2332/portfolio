//
//  ResourceCloud.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/11/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef ResourceCloud_hpp
#define ResourceCloud_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>

using namespace cugl;

class ResourceCloud {
public:
    
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(ResourceCloud);
    
protected:
    int _timeInterval;
    Vec2 _pos;
    
public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new ResourceCloud.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    ResourceCloud(void) { }
    
    /**
     * Destroys this ResourceCloud, releasing all resources.
     */
    virtual ~ResourceCloud(void) { dispose(); }
    
    /**
     * Disposes all resources and assets of this ResourceCloud
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a ResourceCloud may not be used until it is initialized again.
     */
    void dispose();
    
    /**
     * Initializes a new ResourceCloud with the given position and scale
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
    bool init(int x, int y, std::shared_ptr<cugl::Texture> texture, float drawscale);
    
    
#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated ResourceCloud with the given position
     *
     * The ResourceCloud is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     *
     * @return a newly allocated Pest with the given position
     */
    static std::shared_ptr<ResourceCloud> alloc(int x, int y, std::shared_ptr<cugl::Texture> texture, float drawscale) {
        std::shared_ptr<ResourceCloud> result = std::make_shared<ResourceCloud>();
        return (result->init(x, y, texture, drawscale) ? result : nullptr);
    }
    
    void setSceneNode(const std::shared_ptr<cugl::Node>& node);

#pragma mark -
#pragma mark Accessors
    Vec2 getPosition() {return _pos;}
    void setPosition(Vec2 p) {_pos = p;}
    
    int getTimeInterval() {return _timeInterval;}
    void setTimeInterval(int h) {_timeInterval = h;}
    
    void update();
};
#endif /* ResourceCloud_hpp */
