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

ParticleGenerator::ParticleGenerator(std::shared_ptr<cugl::Texture> texture, std::shared_ptr<Obstacle> object, GLuint amount): object(object), texture(texture), amount(amount)
{
    CULogGLError();
    this->init();
}

void ParticleGenerator::Update(GLfloat dt, GLuint newParticles, Vec2 offset){
    // Add new particles
    for (GLuint i = 0; i < newParticles; ++i){
        int unusedParticle = this->firstUnusedParticle();
        this->respawnParticle(this->particles[unusedParticle], offset);
    }
    // Update all particles
//    for (GLuint i = 0; i < this->amount; ++i){
//        Particle &p = this->particles[i];
//        p.life -= dt; // reduce life
//        if (p.life > 0.0f){    // particle is alive, thus update
//            p.position -= p.velocity * dt;
//            p.color.w -= dt * 2.5;
//        }
//    }
}

// Render all particles
void ParticleGenerator::Draw(){
    CULog("Begin Draw");
    if (this->shader->isReady()){
        CULog("I'm ready");
        // Use additive blending to give it a 'glow' effect
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        CULogGLError();
        GLint id;
        glGetIntegerv(GL_CURRENT_PROGRAM,&id);
        CULog("Was bound at %d",id);
        this->shader->bind();
        glGetIntegerv(GL_CURRENT_PROGRAM,&id);
        CULog("Now bound at %d",id);

        CULogGLError();
        for (Particle particle : this->particles){
            if (particle.life > 0.0f){
                this->shader->SetVector2f("offset", particle.position);
                CULogGLError();
                this->shader->SetVector4f("color", particle.color);
                this->shader->setTexture(texture);
                CULogGLError();
                glBindVertexArray(this->VAO);
                CULogGLError();
                glDrawArrays(GL_TRIANGLES, 0, 6);
                CULogGLError();
                glBindVertexArray(0);
                CULogGLError();
            }
        }
        // Don't forget to reset to default blending mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        CULogGLError();
//        this->shader->unbind();
        CULogGLError();
    }
}

void ParticleGenerator::init(){
    this->shader = make_shared<ParticleShader>();
    CULogGLError();
    this->shader->init();
//    shader->bind();
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
    CULogGLError();
    glGenVertexArrays(1, &this->VAO);
    glGenBuffers(1, &VBO);
    glBindVertexArray(this->VAO);
    CULogGLError();
    // Fill mesh buffer
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(particle_quad), particle_quad, GL_STATIC_DRAW);
    CULogGLError();
    // Set mesh attributes
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
    glBindVertexArray(0);
    CULogGLError();
//    shader->attach(this->VAO, VBO);
//    shader->unbind();
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

void ParticleGenerator::respawnParticle(Particle &particle, Vec2 offset){
    GLfloat random = ((rand() % 100) - 50) / 10.0f;
    GLfloat rColor = 0.5 + ((rand() % 100) / 100.0f);
    particle.position = Vec2(500,500);// object->getPosition() + Vec2(random,random) + offset;
    particle.color = Vec4(rColor, rColor, rColor, 1.0f);
    particle.life = 1.0f;
    particle.velocity = Vec2(5.0f,5.0f);
}
