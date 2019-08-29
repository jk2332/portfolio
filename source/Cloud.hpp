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
#include "CloudNode.hpp"
#include "Constants.hpp"

#define BODY           0
//#define UP           1
#define LEFT       1
#define RIGHT      2
//#define DOWN        4

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
    cugl::Vec2 _targetPos;
    
    Vec2 _disp;
    
    // Represents the box obstacle representing the cloud
    std::shared_ptr<BoxObstacle> _ob;
    std::string _cloudTexture;

    /** The scale between the physics world and the screen (MUST BE UNIFORM) */
    float _drawscale;
    int _unitNum;
    bool _contacting;
    b2World* _world;
    bool _isRaining;
    long _rainCoolDown;
    bool _isRainCloud;
    int _type;
    int _id;
    Vec2 _velocity;
    float _cloudSizeScale;
    float _scale;
    std::shared_ptr<ActionManager> _actions;
    std::shared_ptr<ActionManager> _actions2;
    std::shared_ptr<Animate> _rain;
    std::shared_ptr<Animate> _lightning;
    std::shared_ptr<AnimationNode> _rain_node;
    std::shared_ptr<AnimationNode> _lightning_node;

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
    
    bool isRainCloud(){return _isRainCloud;}
    void setTargetPos(Vec2 pos) {_targetPos = pos;}
    Vec2* getTargetPos() {return &_targetPos;}
//    void setIsRainCloud() {
//        if (_cloudSizeScale >= 1.414) {
//            _isRainCloud = true;
//        }
//        else {
//            _isRainCloud = false;
//        }
//    }
    void setIsRaining(bool b){
        _isRaining = b;
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

    void setCloudSizeScale(float s);
    

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
    
    vector<shared_ptr<Node>> setSceneNode(const shared_ptr<cugl::CloudNode>& node, Vec2 displacement, shared_ptr<Texture> cloudFace, shared_ptr<Texture> shadow, shared_ptr<Texture> rain, shared_ptr<Texture> lightning);
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
    void setDrawScale(float drawscale);
    void toggleRain(std::shared_ptr<Sound> source);
    void setId(int id){_id = id;}
    int getId(){return _id;}
    bool isRaining(){return _isRaining;}
    long getRainCoolDown(){return _rainCoolDown;}
    bool shadowCheck(shared_ptr<Node> worldNode, shared_ptr<Node> gridNode);
    bool lightningCheck(shared_ptr<Node> worldNode, shared_ptr<Node> rootNode, shared_ptr<Node> pestNode);
    float getCloudSizeScale() {
        return _cloudSizeScale;
    }
    Vec2 getCloudSize(){
        return Vec2(_cloudSizeScale*ORIGINAL_SIZE_X, _cloudSizeScale*ORIGINAL_SIZE_Y);
    }
    std::shared_ptr<PolygonNode> getShadowNode(){
        return _shadowNode;
    }

    void setLightning();

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
