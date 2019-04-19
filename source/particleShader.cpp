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
#define OPACITY_UNIFORM     "opacity"
#define TEXTURE_POSITION    0

using namespace cugl;

ParticleGenerator::ParticleGenerator(){}

ParticleGenerator::ParticleGenerator(GLuint amount): amount(amount){
    CULogGLError();
    for (int i = 0; i < cloudSections.size(); i++){
        Vec3 currentCircle = cloudSections[i];
        int trueAmount = this->amount;
        //Create twice as many particles for the core section
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            //Determine radius in range 0 to radius of currentCircle
            float r = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/currentCircle.x));
            float t = 0.25*M_PI*j;
            int spacing = 15;
            //center particles on the cloud
            Vec2 centering = Vec2(-10,-10);
            Vec2 offset = spacing*Vec2(r*cos(t) + currentCircle.y, r*sin(t) + currentCircle.z);
            offset = offset + centering;
            
            //Determine v1 and v2 in range 0 to maxVelocity
            float v1 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
            float v2 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
            Vec2 velocity = Vec2(v1 - maxVelocity/2, v2 - maxVelocity/2);
            
            this->particles.push_back(CloudParticle(offset, velocity));
        }
    }
    
}

void ParticleGenerator::Update(GLfloat dt, GLuint newParticles, Vec2 cloud_pos){
//  Update all particles
    for (int i = 0; i < cloudSections.size(); i++){
        int trueAmount = this->amount;
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            int index = trueAmount*i + j;
            if (i != 0){index = index + this->amount;}
            
            CloudParticle &p = this->particles[index];
            Vec2 basePosition = cloud_pos + p.offset;
            
            if ((p.position.x > basePosition.x + maxJostle || p.position.y > basePosition.y + maxJostle) ||
                (p.position.x < basePosition.x - maxJostle || p.position.y < basePosition.y - maxJostle)){
                p.jostle = Vec2::ZERO;
                //Determine v1 and v2 in range 0 to maxVelocity
                float v1 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
                float v2 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
                p.velocity = Vec2(v1 - maxVelocity/2, v2 - maxVelocity/2);
            }
            else{
                p.jostle += dt*p.velocity;
            }
            p.position = basePosition + p.jostle;
            
            float greater = p.jostle.x;
            if (p.jostle.y > p.jostle.x){
                greater = p.jostle.y;
            }
            p.opacity = 1.0 - abs(greater/maxJostle);
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

void ParticleGenerator::respawnParticle(CloudParticle &particle, Vec2 offset){
    particle.position = Vec2(500,500);
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
    
    //Placeholder to adjust size of particles based on scale of cloud
    
    
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
            SetVector4f(COLOR_UNIFORM, Vec4(p.opacity, 0.0, 0.0, 0.0));
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
