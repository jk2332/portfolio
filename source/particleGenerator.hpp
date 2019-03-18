//
//  particleGenerator.hpp
//  WeatherDefender (Mac)
//
//  Based on an example from learnopengl.com
//  Created by Stefan Joseph on 3/10/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef particleGenerator_hpp
#define particleGenerator_hpp

#include <stdio.h>
#include <vector>
#include <cugl/cugl.h>
#include "Cloud.hpp"
#include "particleShader.h"

using namespace cugl;

// Represents a single particle and its state
struct Particle {
    Vec2 position, velocity;
    float opacity;
    float life;
    Vec4 color;

    Particle() : position(Vec2(0.0f,0.0f)), velocity(Vec2(0.0f,0.0f)), color(Vec4(0.0f,0.0f,0.0f,0.0f)), opacity(1.0f), life(0.0f) { }
};


// ParticleGenerator acts as a container for rendering a large number of
// particles by repeatedly spawning and updating particles and killing
// them after a given amount of time.
class ParticleGenerator
{
public:
    // Constructors
    ParticleGenerator();
    ParticleGenerator(std::shared_ptr<cugl::Texture> texture, GLuint amount);
    // Update all particles
    void Update(GLfloat dt, std::shared_ptr<Cloud> object, GLuint newParticles, Vec2 offset = Vec2(0.0f, 0.0f));
    // Render all particles
    void Draw();
private:
    // State
    std::vector<Particle> particles;
    GLuint amount;
    // Render state
    ParticleShader shader;
    std::shared_ptr<cugl::Texture> texture;
    GLuint VAO;
    // Initializes buffer and vertex attributes
    void init();
    // Returns the first Particle index that's currently unused e.g. Life <= 0.0f or 0 if no particle is currently inactive
    int firstUnusedParticle();
    // Respawns particle
    void respawnParticle(Particle &particle, std::shared_ptr<Cloud> object, Vec2 offset = Vec2(0.0f, 0.0f));
};

#endif /* particleGenerator_hpp */
