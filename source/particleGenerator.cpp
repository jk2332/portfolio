//
//  particleGenerator.cpp
//  WeatherDefender (Mac)
//
//  Based on an example from learnopengl.com
//  Created by Stefan Joseph on 3/10/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "particleGenerator.hpp"
#include "particleShader.h"
#include <cugl/util/CUDebug.h>

ParticleGenerator::ParticleGenerator(){}

ParticleGenerator::ParticleGenerator(std::shared_ptr<cugl::Texture> texture, GLuint amount): texture(texture), amount(amount)
{
    this->init();
}

void ParticleGenerator::Update(GLfloat dt, Cloud &object, GLuint newParticles, Vec2 offset){
    // Add new particles
    for (GLuint i = 0; i < newParticles; ++i)
    {
        int unusedParticle = this->firstUnusedParticle();
        this->respawnParticle(this->particles[unusedParticle], object, offset);
    }
    // Update all particles
    for (GLuint i = 0; i < this->amount; ++i)
    {
        Particle &p = this->particles[i];
        p.life -= dt; // reduce life
        if (p.life > 0.0f)
        {    // particle is alive, thus update
            p.position -= p.velocity * dt;
            p.color.w -= dt * 2.5;
        }
    }
}

// Render all particles
void ParticleGenerator::Draw(){
    CULog("Begin Draw");
    if (this->shader.isReady()){
        CULog("I'm ready");
        // Use additive blending to give it a 'glow' effect
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        _check_gl_error("particleGenerator", 48);
        this->shader.bind();
        for (Particle particle : this->particles){
            if (particle.life > 0.0f){
                this->shader.SetVector2f("offset", particle.position);
                this->shader.SetVector4f("color", particle.color);
                glBindVertexArray(this->VAO);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
                _check_gl_error("particleGenerator", 57);
            }
        }
        // Don't forget to reset to default blending mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        this->shader.unbind();
        _check_gl_error("particleGenerator", 61);
    }
}

void ParticleGenerator::init(){
    this->shader = ParticleShader();
    this->shader.init();
    // Set up mesh and attribute properties
    GLuint VBO;
    GLfloat particle_quad[] = {
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 1.0f, 0.0f
    };
    _check_gl_error("particleGenerator", 81);
    glGenVertexArrays(1, &this->VAO);
    glGenBuffers(1, &VBO);
    glBindVertexArray(this->VAO);
    _check_gl_error("particleGenerator", 85);
    // Fill mesh buffer
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(particle_quad), particle_quad, GL_STATIC_DRAW);
    _check_gl_error("particleGenerator", 89);
    // Set mesh attributes
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
    glBindVertexArray(0);
    _check_gl_error("particleGenerator", 91);

    // Create this->amount default particle instances
    for (GLuint i = 0; i < this->amount; ++i)
        this->particles.push_back(Particle());
}

// Stores the index of the last particle used (for quick access to next dead particle)
int lastUsedParticle = 0;
int ParticleGenerator::firstUnusedParticle(){
    // First search from last used particle, this will usually return almost instantly
    for (int i = lastUsedParticle; i < this->amount; ++i){
        if (this->particles[i].life <= 0.0f){
            lastUsedParticle = i;
            return i;
        }
    }
    // Otherwise, do a linear search
    for (int i = 0; i < lastUsedParticle; ++i){
        if (this->particles[i].life <= 0.0f){
            lastUsedParticle = i;
            return i;
        }
    }
    // All particles are taken, override the first one (note that if it repeatedly hits this case, more particles should be reserved)
    lastUsedParticle = 0;
    return 0;
}

void ParticleGenerator::respawnParticle(Particle &particle, Cloud &object, Vec2 offset)
{
    GLfloat random = ((rand() % 100) - 50) / 10.0f;
    GLfloat rColor = 0.5 + ((rand() % 100) / 100.0f);
    particle.position = object.getPosition() + Vec2(random,random) + offset;
    particle.color = Vec4(rColor, rColor, rColor, 1.0f);
    particle.life = 1.0f;
    particle.velocity = object.getVelocity() * 0.1f;
}
