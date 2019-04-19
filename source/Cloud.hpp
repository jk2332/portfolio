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
#include "CloudNode.hpp"

#define MAX_CLOUD_UNIT  3
#define PART_NONE           -1
#define BODY           0
//#define UP           1
#define LEFT       1
#define RIGHT      2
//#define DOWN        4
/** Distance between torso center and face center */
#define TORSO_OFFSET    3.8f
/** The density for each body part */
#define DEFAULT_DENSITY  1.0f
/** The density for the center of mass */
#define CENTROID_DENSITY 1f
/** The radius for the center of mass */
#define CENTROID_RADIUS  0.1f
#define ORIGINAL_SIZE_X    165
#define ORIGINAL_SIZE_Y    84

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
class Cloud : public PolygonObstacle {
private:
    /** This macro disables the copy constructor (not allowed on scene graphs) */
    CU_DISALLOW_COPY_AND_ASSIGN(Cloud);

protected:
    /** Shape to treat the root body as a center of mass */
    b2Fixture* _centroid;
    std::shared_ptr<cugl::Texture> _texture;

    /** The scene graph node for the Cloud. Contains the cloud's face. */
    std::shared_ptr<CloudNode> _cloudNode;
    
    /** The scene graph node for the Cloud. Contains the cloud's shadow. */
    std::shared_ptr<PolygonNode> _shadowNode;
    
    std::shared_ptr<Node> _node;

    float _disp;
    
    // Represents the box obstacle representing the cloud
    std::shared_ptr<BoxObstacle> _ob;

    std::string _cloudTexture;
    
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
    int _id;
    Vec2 _velocity;
    float _sizeLevel;
    float _scale;
    
//    /**
//     * Returns the texture key for the given body part.
//     *
//     * As some body parts are symmetrical, we reuse textures.
//     *
//     * @return the texture key for the given body part
//     */
//    std::string getPartName(int part);

public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new Ragdoll at the origin.
     *
     * NEVER USE A CONSTRUCTOR WITH NEW. If you want to allocate a model on
     * the heap, use one of the static constructors instead.
     */
    Cloud(void) : PolygonObstacle() { }

    /**
     * Destroys this Ragdoll, releasing all resources.
     */
    virtual ~Cloud(void) {
//        CULog("CLOUD DESTRUCTOR CALLED");
        dispose(); }

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
//    virtual bool init() override { return init(cugl::Poly2::Poly2()); }

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
    bool init(const cugl::Poly2, const cugl::Vec2);

    std::shared_ptr<BoxObstacle> getObstacle();

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
//    static std::shared_ptr<Cloud> alloc() {
//        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
//        return (result->init(cugl::Poly2::Poly2()) ? result : nullptr);
//    }

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
    static std::shared_ptr<Cloud> alloc(Poly2 p) {
        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
        return (result->init(p, Vec2(0.5f, 0.5f)) ? result : nullptr);
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
    static std::shared_ptr<Cloud> alloc(Poly2 p, const cugl::Vec2& pos) {
        std::shared_ptr<Cloud> result = std::make_shared<Cloud>();
        return (result->init(p, pos) ? result : nullptr);
    }
    
#pragma mark -
#pragma mark Animation
	/**
	* Returns the texture (key) for this crate
	*
	* The value returned is not a Texture2D value.  Instead, it is a key for
	* accessing the texture from the asset manager.
	*
	* @return the texture (key) for this crate
	*/
	const std::string& getTextureKey() const { return _cloudTexture; }

	/**
	* Sets the texture (key) for this crate
	*
	* The value returned is not a Texture2D value.  Instead, it is a key for
	* accessing the texture from the asset manager.
	*
	* @param  strip    the texture (key) for this crate
	*/
	void setTextureKey(std::string strip) { _cloudTexture = strip; }
    
#pragma mark Physics Methods    
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
    
#pragma mark Physics Methods

    bool dropUnit(b2World& world);

    void markForRemoval();

    void setScale(float s);

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

    void setCloudSizeLevel(float s) {_sizeLevel = s;}

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
    const std::shared_ptr<CloudNode>& getCloudNode() const { return _cloudNode; }

    const std::shared_ptr<Node>& getNode() const { return _node; }


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
    
    void setSceneNodeParticles(const std::shared_ptr<cugl::CloudNode>& node, float displacement,
                               std::shared_ptr<Texture> cloudFace, std::shared_ptr<Texture> shadow);
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
    void setId(int id){_id = id;}
    int getId(){return _id;}
    bool isRaining(){return _isRaining;}
    long getRainCoolDown(){return _rainCoolDown;}
    void setSizeLevel(float f);
    bool shadowCheck(shared_ptr<Node> worldNode, shared_ptr<Node> gridNode);
    float getCloudSizeLevel() {
        return _sizeLevel;
    }
    Vec2 getCloudSize(){
        return Vec2(_sizeLevel*0.5*ORIGINAL_SIZE_X, _sizeLevel*0.5*ORIGINAL_SIZE_Y);
    }


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
