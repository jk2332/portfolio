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

ParticleGenerator::ParticleGenerator(GLuint amount): amount(amount), timeSinceLastJostle(0){
    CULogGLError();
    for (int i = 0; i < cloudSections.size(); i++){
        Vec3 currentCircle = cloudSections[i];
        int trueAmount = this->amount;
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            //Determine radius in range 0 to radius of currentCircle
            float r = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/currentCircle.x));
            float t = 0.25*M_PI*j;
            int spacing = 15;
            Vec2 centering = Vec2(-10,-10);
            Vec2 offset = spacing*Vec2(r*cos(t) + currentCircle.y, r*sin(t) + currentCircle.z);
            offset = offset + centering;
            this->particles.push_back(CloudParticle(offset));
        }
    }
    
}

void ParticleGenerator::Update(GLfloat dt, GLuint newParticles, Vec2 cloud_pos){
    timeSinceLastJostle += dt;
    bool jostled = false;
//  Update all particles
    for (int i = 0; i < cloudSections.size(); i++){
        int trueAmount = this->amount;
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            int index = trueAmount*i + j;
            if (i != 0){index = index + this->amount;}
            
            //Determine j1 and j2 in range 0 to maxJostle
            float j1 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxJostle));
            float j2 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxJostle));
            
            CloudParticle &p = this->particles[index];
            if (timeSinceLastJostle > 0.2){
                jostled = true;
                p.jostleAmount = Vec2(j1 - maxJostle/2, j2 - maxJostle/2);
                if ((p.jostleAmount.x > maxJostle && p.jostleAmount.y > maxJostle) ||
                    (p.jostleAmount.x < -maxJostle && p.jostleAmount.y < -maxJostle)){
                    p.jostleAmount = Vec2::ZERO;
                }
            }
            p.position = cloud_pos + p.offset + p.jostleAmount;
        }
    }
    if(jostled){timeSinceLastJostle = 0.0;}
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

void ParticleGenerator::respawnParticle(CloudParticle &particle, Vec2 offset){
    GLfloat random = ((rand() % 100) - 50) / 10.0f;
    GLfloat rColor = 0.5 + ((rand() % 100) / 100.0f);
    particle.position = Vec2(500,500);// object->getPosition() + Vec2(random,random) + offset;
    particle.color = Vec4(rColor, rColor, rColor, 1.0f);
    particle.life = 1.0f;
    particle.jostleAmount = Vec2(5.0f,5.0f);
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
    particleTexture = Texture();
    particleTexture.initWithFile("textures/particle1.png");
    
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

// Render all particles
void ParticleShader::drawParticles(){
    CULogGLError();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE);
    CULogGLError();
    //reusing the particle shader program
    glUseProgram( _program );
//    CULog("Program is %d",_program);
    CULogGLError();
    glGenBuffers(1, &VBO);
    CULogGLError();
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
//    CULog("Variable address is %d",_aPosition);
    glEnableVertexAttribArray(_aPosition);
    CULogGLError();
    glVertexAttribPointer(_aPosition, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
    CULogGLError();
    
    _aTexCoords = glGetAttribLocation(_program, TEXCOORDS_ATTRIBUTE);
    CULogGLError();
    glEnableVertexAttribArray(_aTexCoords);
    CULogGLError();
    glVertexAttribPointer(_aTexCoords, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (void*)(2 * sizeof(GLfloat)));
    CULogGLError();
    
    particleTexture.bind();
    _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM );
    CULogGLError();
    glUniform1i(_uSprite, TEXTURE_POSITION);
    CULogGLError();

    for (CloudParticle p : _pg.particles){
        if (p.life > 0.0f){
            SetVector2f(OFFSET_UNIFORM, p.position);
            //SetVector4f(COLOR_UNIFORM, Vec4(0.0,0.0,0.0,1.0));
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            CULogGLError();
        }
    }
    particleTexture.unbind();
    CULogGLError();
    glDisableVertexAttribArray(_aPosition);
    CULogGLError();
    glDisableVertexAttribArray(_aTexCoords);
    CULogGLError();
    glUseProgram(NULL);
    CULogGLError();
    // Don't forget to reset to default blending mode
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    CULogGLError();
}

void ParticleShader::update(Vec2 cloud_pos, float dt, GLuint np){
    this->_pg.Update(dt, np, cloud_pos - Vec2(500,300));
}
