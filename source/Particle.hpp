//
//  Particle.cpp
//  MemoryDemo
//
//  This file provides a pair of classes: a Particle and a ParticleNode.  The
//  ParticleNode is used to draw the Particle as part of a scene graph.  For
//  efficient reasons, you have many Particles, but only one ParticleNode.
//
//  Author: Walker White and Anthony Perello
//  Version: 2/17/17
//
#ifndef __MD_PARTICLE_H__
#define __MD_PARTICLE_H__

#include "cugl/cugl.h"
#include <set>

using namespace cugl;

/** How many pixels the particle moves per animation frame */
#define PARTICLE_SPEED 2.0f

#pragma mark -
#pragma mark Particle

/**
 * This class represents a simple particle
 *
 * Particles only contain position information.  They do not have any
 * drawing information. We use a single ParticleNode to draw a collection
 * of Particles.
 *
 * The Particle class implements the reset() method, making it compatible
 * with the FreeList template.
 */
class Particle {
private:
    /** The particle position */
    Vec2 _position;
    /** The particle velocity (not directly accessible) */
    Vec2 _velocity;
    /** The particle angle of movement (according to initial position) */
    float _trajectory;
    /** Whether or not the particle is visible */
    bool _visible;
    
public:
    /** Construct a particle with default values */
    Particle() : _trajectory(0), _visible(false) {}
    
    /**
     * Returns the position of this particle
     *
     * @return the position of this particle
     */
    Vec2 getPosition() const { return _position; }
    
    /**
     * Sets the position of this particle
     *
     * @param pos   The position of this particle
     */
    void setPosition(Vec2 pos) { _position = pos; }
    
    /**
     * Returns the trajectory angle of this particle
     *
     * The particle velocity is (PARTICLE_SPEED,angle) in polar-coordinates.
     *
     * @return the trajectory angle of this particle
     */
    float getTrajectory() const { return _trajectory; }
    
    /**
     * Sets the trajectory angle of this particle
     *
     * When the angle is set, the particle will change its velocity
     * to (PARTICLE_SPEED,angle) in polar-coordinates.
     *
     * @param angle  the trajectory angle of this particle
     */
    void setTrajectory(float angle);
    
    /**
     * Move the particle one frame, adding the velocity to the position.
     */
    void move() { _position += _velocity; }
    
    /**
     * Returns true if this particle is visible
     *
     * @return true if this particle is visible
     */
    bool isVisible() const { return _visible; }
    
    /**
     * Sets whether this particle is visible
     *
     * @param flag  Whether this particle is visible
     */
    void setVisible(bool visible) { _visible = visible; }
    
    /** Resets a previously allocated particle */
    void reset();
};


#pragma mark -
#pragma mark ParticleNode
/**
 * A scene graph node for a list of particles.
 *
 * For this demo, we do not need any physics.  It is fine enough to draw.  We
 * only need one ParticleNode for a collection of particles.
 */
class ParticleNode : public PolygonNode {
private:
    /** Container to store the particles currently allocated */
    std::set<Particle*> _particles;
    
public:
    /**
     * Returns a new ParticleNode with the given texture
     *
     * All particles will use the given texture as their representation. The
     * particles will have the natural size of this texture; individual particles
     * cannot be scaled.  For more control, you will need to implement your
     * own particle node.
     *
     * @param texture   A shared pointer to a Texture object.
     *
     * @return a new ParticleNode with the given texture
     */
    static std::shared_ptr<ParticleNode> allocWithTexture(const std::shared_ptr<Texture>& texture) {
        std::shared_ptr<ParticleNode> node = std::make_shared<ParticleNode>();
        return (node->initWithTexture(texture) ? node : nullptr);
    }
    
    /**
     * Returns the number of particles in this node
     *
     * @return the number of particles in this node
     */
    size_t getParticleCount() { return _particles.size(); }
    
    /**
     * Adds a particle to this node
     *
     * If the particle is already in the node, nothing happens
     *
     * @param particle  The particle to add to this node
     */
    void addParticle(Particle* particle) {
        _particles.insert(particle);
    }
    
    /**
     * Removes a particle from this node
     *
     * If the particle is not in the node, nothing happens
     *
     * @param particle  The particle to remove from this node
     */
    void removeParticle(Particle* particle) {
        _particles.erase(particle);
    }
    
    /**
     * Updates all of the particles, moving them one time step
     *
     * If a particle goes out of the bounding box of this node, it is added
     * to the reset set.  This allows the caller a chance to remove and
     * recycle these particles.
     *
     * @param reset The recycle set
     */
    void update(std::set<Particle*>& reset);
    
    /**
     * Draws this Node via the given SpriteBatch.
     *
     * This method only worries about drawing the current node.  It does not
     * attempt to render the children.
     *
     * This is the method that you should override to implement your custom
     * drawing code.  You are welcome to use any OpenGL commands that you wish.
     * You can even skip use of the SpriteBatch.  However, if you do so, you
     * must flush the SpriteBatch by calling end() at the start of the method.
     * in addition, you should remember to call begin() at the start of the
     * method.
     *
     * This method provides the correct transformation matrix and tint color.
     * You do not need to worry about whether the node uses relative color.
     * This method is called by render() and these values are guaranteed to be
     * correct.  In addition, this method does not need to check for visibility,
     * as it is guaranteed to only be called when the node is visible.
     *
     * @param batch     The SpriteBatch to draw with.
     * @param transform The global transformation matrix.
     * @param tint      The tint to blend with the Node color.
     */
    virtual void draw(const std::shared_ptr<SpriteBatch>& batch, const Mat4& transform, Color4 tint) override;
    
};

#endif /* defined(__MD_PARTICLE_H__) */

