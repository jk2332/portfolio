//
//  Cloud.hpp
//  Weather Defender (Mac)
//
//  Created by 김지원 on 2/25/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Cloud_hpp
#define Cloud_hpp

#include <stdio.h>
#include <cugl/cugl.h>
#include <vector>
#include "BubbleGenerator.h"

#define MAX_CLOUD_UNIT  3
#define PART_NONE           -1
#define BODY           0
//#define UP           1
#define LEFT       1
#define RIGHT      2
//#define DOWN        4
/** Distance between torso center and face center */
#define TORSO_OFFSET    3.8f
/** Y-distance between torso center and arm center */
#define ARM_YOFFSET     0.0f
/** X-distance between torso center and arm center */
#define ARM_XOFFSET     4.0f
/** Distance between center of arm and center of forearm */
#define FOREARM_OFFSET  2.75f
/** X-distance from center of torso to center of leg */
#define THIGH_XOFFSET   0.75f
/** Y-distance from center of torso to center of thigh */
#define THIGH_YOFFSET   3.5f
/** Distance between center of thigh and center of shin */
#define SHIN_OFFSET     2.25f
/** The density for each body part */
#define DEFAULT_DENSITY  1.0f
/** The density for the center of mass */
#define CENTROID_DENSITY 0.1f
/** The radius for the center of mass */
#define CENTROID_RADIUS  0.1f


#pragma mark -
#pragma mark Ragdoll
/**
 * A ragdoll whose body parts are boxes connected by joints
 *
 * Note that this module handles its own scene graph management.  Since a
 * ComplexObstacle owns all of its child obstacles, it is natural for it to
 * own the corresponding as well scene graph.
 *
 * For the construction, see the ragdoll diagram above, with the position offsets.
 */
class Cloud : public ComplexObstacle {
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Cloud);
    
protected:
    /** Shape to treat the root body as a center of mass */
    b2Fixture* _centroid;
//    /** Bubble generator to glue to snorkler. */
//    std::shared_ptr<BubbleGenerator> _bubbler;
    /** The textures for the individual body parts */
    std::shared_ptr<cugl::Texture> _texture;
    
    /** The scene graph node for the Ragdoll. This is empty, but attaches parts to it. */
    std::shared_ptr<cugl::Node> _node;
    
//    /** Cache object for transforming the force according the object angle */
//    cugl::Mat4 _affine;
    /** The scale between the physics world and the screen (MUST BE UNIFORM) */
    float _drawscale;
    int _unitNum;
    bool _contacting;
    b2World* _world;
    bool _isRaining;
    long _rainCoolDown;
    int _type;
    Vec2 _velocity;
    float _size;
    
//    /**
//     * Returns the texture key for the given body part.
//     *
//     * As some body parts are symmetrical, we reuse textures.
//     *
//     * @return the texture key for the given body part
//     */
//    std::string getPartName(int part);
    
    /**
     * Returns a single body part
     *
     * While it looks like this method "connects" the pieces, it does not really.
     * It puts them in position to be connected by joints, but they will fall apart
     * unless you make the joints.
     *
     * @param  part     Part to create
     * @param  connect  Part to connect it to
     * @param  pos      Position RELATIVE to connecting part
     * @param  scale    The draw scale to convert from world to screen coordinates
     *
     * @return the created body part
     */
    std::shared_ptr<cugl::BoxObstacle> makeUnit(int part, int connect, const cugl::Vec2& pos);
    
public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Ragdoll at the origin.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Cloud(void) : ComplexObstacle() { }
    
    /**
     * Destroys this Ragdoll, releasing all resources.
     */
    virtual ~Cloud(void) { dispose(); }
    
    void setWorld(b2World& _world);

    /**
     * Disposes all resources and assets of this Ragdoll
     *
     * Any assets owned by this object will be immediately released.  Once
     * disposed, a Ragdoll may not be used until it is initialized again.
     */
    void dispose();
    
    /**
     * Initializes a new Ragdoll at the origin.
     *
     * The Ragdoll is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @return  true if the obstacle is initialized properly, false otherwise.
     */
    virtual bool init() override { return init(cugl::Vec2::ZERO, 1.0f); }
    
    /**
     * Initializes a new Ragdoll with the given position
     *
     * The Ragdoll is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     *
     * @return  true if the obstacle is initialized properly, false otherwise.
     */
    bool init(const cugl::Vec2& pos) override { return init(pos,1.0f); }
    
    /**
     * Initializes a new Ragdoll with the given position and scale
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
    bool init(const cugl::Vec2& pos, float scale);
    
    
#pragma mark -
#pragma mark Static Constructors
    /**
     * Returns a newly allocated Ragdoll at the origin.
     *
     * The Ragdoll is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @return a newly allocated Ragdoll at the origin.
     */
    static std::shared_ptr<Cloud> alloc() {
        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
        return (result->init() ? result : nullptr);
    }
    
    /**
     * Returns a newly allocated Ragdoll with the given position
     *
     * The Ragdoll is scaled so that 1 pixel = 1 Box2d unit
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     *
     * @return a newly allocated Ragdoll with the given position
     */
    static std::shared_ptr<Cloud> alloc(const cugl::Vec2& pos) {
        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
        return (result->init(pos, 1.0f) ? result : nullptr);
    }
    
    /**
     * Returns a newly allocated Ragdoll with the given position and scale
     *
     * The scene graph is completely decoupled from the physics system.
     * The node does not have to be the same size as the physics body. We
     * only guarantee that the scene graph node is positioned correctly
     * according to the drawing scale.
     *
     * @param pos   Initial position in world coordinates
     * @param scale The drawing scale to convert world to screen coordinates
     *
     * @return a newly allocated Ragdoll with the given position
     */
    static std::shared_ptr<Cloud> alloc(const cugl::Vec2& pos, float scale) {
        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
        return (result->init(pos, scale) ? result : nullptr);
    }
    
    
#pragma mark Physics Methods
    /**
     * Creates the joints for this object.
     *
     * This method is executed as part of activePhysics. This is the primary method to
     * override for custom physics objects.
     *
     * @param world Box2D world to store joints
     *
     * @return true if object allocation succeeded
     */
    bool createJoints(b2World& world) override;
    
    /**
     * Create new fixtures for this body, defining the shape
     *
     * This method is typically undefined for complex objects.  While they
     * need a root body, they rarely need a root shape.  However, we provide
     * this method for maximum flexibility.
     */
    virtual void createFixtures() override;
    
    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This method is typically undefined for complex objects.  While they
     * need a root body, they rarely need a root shape.  However, we provide
     * this method for maximum flexibility.
     */
    virtual void releaseFixtures() override;
    
    
    b2Fixture* GetFixtureA();
    
    // Get the second fixture in this contact
    b2Fixture* GetFixtureB();
    
    /**
     * Creates the individual body parts for this ragdoll
     *
     * The size of the body parts is determined by the scale together with
     * the assets (as part of the asset manager).  This will fail if any
     * body part assets are missing.
     *
     * @param assets The program asset manager
     *
     * @return true if the body parts were successfully created
     */
    bool initialBuild(const std::shared_ptr<AssetManager>& assets);
    
    bool dropUnit(b2World& world);
    
    bool joinUnit(b2World& world);

    void markForRemoval();
    
    
#pragma mark -
#pragma mark Attribute Accessors
    /**
     * Returns the texture for the given body part.
     *
     * As some body parts are symmetrical, we may reuse textures.
     *
     * @return the texture for the given body part
     */
    const std::shared_ptr<Texture> getTexture() const {
        return _texture;
    }
    
    /**
     * Sets the texture for the given body part.
     *
     * As some body parts are symmetrical, we may reuse textures.
     *
     * @param part      The part identifier
     * @param texture   The texture for the given body part
     */
    void setTexture(const std::shared_ptr<Texture>& texture);
    
//    /**
//     * Returns the bubble generator for this ragdoll
//     *
//     * The bubble generator will be offset at the snorkel on the head.
//     *
//     * @return the bubble generator for this ragdoll
//     */
//    const std::shared_ptr<BubbleGenerator> getBubbleGenerator() const {
//        return _bubbler;
//    }
//
//    /**
//     * Creates the bubble generator for this ragdoll
//     *
//     * The bubble generator will be offset at the snorkel on the head.
//     *
//     * @param texture   The texture for an individual bubble
//     */
//    void makeBubbleGenerator(const std::shared_ptr<Texture>& bubble);
    
    float getSize() {return _size;}
    void setSize(float s) {_size = s;}
    
    Vec2 getVelocity() {return _velocity;}
    void setVelocity(Vec2 v) {_velocity = v;}
    
#pragma mark -
#pragma mark Animation
    /**
     * Returns the scene graph node representing this Ragdoll.
     *
     * By storing a reference to the scene graph node, the model can update
     * the node to be in sync with the physics info. It does this via the
     * {@link Obstacle#update(float)} method.
     *
     * @return the scene graph node representing this Ragdoll.
     */
    const std::shared_ptr<cugl::Node>& getNode() const { return _node; }

    
    /**
     * Sets the scene graph node representing this Ragdoll.
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
     * @param node  The scene graph node representing this Ragdoll, which has been added to the world node already.
     */
    void setSceneNode(const std::shared_ptr<cugl::Node>& node);
    
    void makeRainDrops(cugl::Vec2& pos, std::shared_ptr<cugl::Texture> rainTexture);
    
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
    void setIsRaining(float b){_isRaining = b;}
    bool getIsRaining(){return _isRaining;}
    long getRainCoolDown(){return _rainCoolDown;}
    void incSize(float f);
    void decSize();

    
#pragma mark -
#pragma mark Physics
    
    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * This method is called AFTER the collision resolution state. Therefore, it
     * should not be used to process actions or any other gameplay information.
     * Its primary purpose is to adjust changes to the fixture, which have to
     * take place after collision.
     *
     * In other words, this is the method that updates the scene graph.  If you
     * forget to call it, it will not draw your changes.
     *
     * @param delta Timing values from parent loop
     */
    virtual void update(float delta) override;
};

#endif 
