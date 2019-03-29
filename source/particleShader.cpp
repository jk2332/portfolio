//
//  particleShader.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/28/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "particleShader.hpp"

#define POSITION_ATTRIBUTE  "position"
#define TEXCOORDS_ATTRIBUTE  "texCoords"
#define PROJECTION_UNIFORM  "projection"
#define OFFSET_UNIFORM  "offset"
#define COLOR_UNIFORM     "color"
#define SPRITE_UNIFORM     "sprite"
#define TEXTURE_POSITION    0

using namespace cugl;

ParticleGenerator::ParticleGenerator(){}

ParticleGenerator::ParticleGenerator(std::shared_ptr<Obstacle> object, GLuint amount): object(object), amount(amount){
    CULogGLError();
    for (GLuint i = 0; i < this->amount; ++i)
        this->particles.push_back(Particle());
}

void ParticleGenerator::Update(GLfloat dt, GLuint newParticles, Vec2 offset){
    // Add new particles
    for (GLuint i = 0; i < newParticles; ++i){
        int unusedParticle = this->firstUnusedParticle();
        this->respawnParticle(this->particles[unusedParticle], offset);
    }
//     Update all particles
        for (GLuint i = 0; i < this->amount; ++i){
            Particle &p = this->particles[i];
            p.life -= dt; // reduce life
            p.velocity = object->getPosition() - p.position;
            if (p.life > 0.0f){    // particle is alive, thus update
                p.position = p.position + p.velocity * dt;
                p.color.w -= dt * 2.5;
            }
        }
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

void ParticleShader::dispose() {
    Shader::dispose();
    //glDeleteTextures(1, texture);
    glDeleteProgram(_program);
    CULogGLError();
    glDeleteShader(_fragShader);
    CULogGLError();
    glDeleteShader(_vertShader);
    CULogGLError();
    glDeleteBuffers(1, &EBO);
    CULogGLError();
    glDeleteBuffers(1, &VBO);
    CULogGLError();
    glDeleteVertexArrays(1, &VAO);
    CULogGLError();
}

void ParticleShader::onStartup(){
    particle = Texture();
    particle.initWithFile("textures/particle1.png");
    
    compileProgram();
}

void ParticleShader::compileProgram(){
    _vertSource = oglCTVert;
    _fragSource = oglCTFrag;
    _program = glCreateProgram();
    if (!_program) {
        CULogError("Unable to allocate shader program");
    }
    CULogGLError();
    
    //Create vertex shader and compile it
    _vertShader = glCreateShader( GL_VERTEX_SHADER );
    glShaderSource( _vertShader, 1, &_vertSource, nullptr );
    glCompileShader( _vertShader );
    
    CULogGLError();
    // Validate and quit if failed
    if (!validateShader(_vertShader, "vertex")) {
        dispose();
    }
    
    CULogGLError();
    //Create fragment shader and compile it
    _fragShader = glCreateShader( GL_FRAGMENT_SHADER );
    glShaderSource( _fragShader, 1, &_fragSource, nullptr );
    glCompileShader( _fragShader );
    
    // Validate and quit if failed
    if (!validateShader(_fragShader, "fragment")) {
        dispose();
    }
    
    CULogGLError();
    // Now kiss
    glAttachShader( _program, _vertShader );
    glAttachShader( _program, _fragShader );
    glLinkProgram( _program );
    
    //Check for errors
    GLint programSuccess = GL_TRUE;
    glGetProgramiv( _program, GL_LINK_STATUS, &programSuccess );
    if( programSuccess != GL_TRUE ) {
        CULogError( "Unable to link program %d.\n", _program );
        dispose();
    }
}

//do the thing
void ParticleShader::drawParticle(Vec2 pos){
    CULogGLError();
    // Set mesh attributes
    
    //glBlendFunc(GL_SRC_ALPHA, GL_ONE);
    //CULogGLError();
    
    //reusing the particle shader program
    glUseProgram( _program );
    CULog("Program is %d",_program);
    CULogGLError();
    
//    glGenVertexArrays(1, &VAO);
    CULogGLError();
//    glBindVertexArray(VAO);
    CULogGLError();
    //rebinding presumably because of the spritebatch that left itself bound
    glGenBuffers(1, &VBO);
    glBindBuffer(GL_ARRAY_BUFFER,VBO);
    CULogGLError();
    glBufferData(GL_ARRAY_BUFFER, sizeof(particle_quad), particle_quad, GL_DYNAMIC_DRAW);
    CULogGLError();
    
    glGenBuffers(1, &EBO);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    CULogGLError();
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(elements), elements, GL_STATIC_DRAW);
    CULogGLError();
    
    _aPosition = glGetAttribLocation(_program, POSITION_ATTRIBUTE);
    CULogGLError();
    CULog("Variable address is %d",_aPosition);
    
    glEnableVertexAttribArray(_aPosition);
    CULogGLError();
    glVertexAttribPointer(_aPosition, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
    CULogGLError();
    
    SetVector2f(OFFSET_UNIFORM, pos);
    //SetVector4f(COLOR_UNIFORM, Vec4(0.0,0.0,0.0,1.0));
    
    _aTexCoords = glGetAttribLocation(_program, TEXCOORDS_ATTRIBUTE);
    CULogGLError();
    glEnableVertexAttribArray(_aTexCoords);
    CULogGLError();
    glVertexAttribPointer(_aTexCoords, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (void*)(2 * sizeof(GLfloat)));
    CULogGLError();
    
    particle.bind();
    _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM );
    CULogGLError();
    glUniform1i(_uSprite, TEXTURE_POSITION);
    CULogGLError();
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    CULogGLError();
    particle.unbind();
//    glBindVertexArray(NULL);
    CULogGLError();
    
    glDisableVertexAttribArray(_aPosition);
    CULogGLError();
    glDisableVertexAttribArray(_aTexCoords);
    CULogGLError();
    
    glUseProgram(NULL);
    CULogGLError();
    // Don't forget to reset to default blending mode
//    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    CULogGLError();
}

void ParticleShader::update(float dt, GLuint np){
    this->_pg.Update(dt, np);
}

// Render all particles
void ParticleShader::draw(){
    CULog("Begin Draw");
    CULogGLError();
    for (Particle particle : this->_pg.particles){
        if (particle.life > 0.0f){
            drawParticle(particle.position);
        }
    }
}
