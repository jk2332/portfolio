//
//  particleShader.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/28/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "particleShader.hpp"

#define POSITION_ATTRIBUTE   "position"
#define TEXCOORDS_ATTRIBUTE  "texCoords"
#define OFFSET_UNIFORM       "offset"
#define COLOR_UNIFORM        "color"
#define ASPECT_UNIFORM       "aspectRatio"
#define SPRITE_UNIFORM       "sprite"
#define TEXTURE_POSITION     0

using namespace cugl;

ParticleGenerator::ParticleGenerator(){}

ParticleGenerator::ParticleGenerator(GLuint amount, float ds): amount(amount){
    CULogGLError();
    drawscale = ds;
    for (int i = 0; i < cloudSections.size(); i++){
        Vec3 currentCircle = cloudSections[i];
        int trueAmount = this->amount;
        //Create twice as many particles for the core section
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            //Determine radius in range 0 to radius of currentCircle
            float r = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/currentCircle.x));
            float t = 0.25*M_PI*j;
            Vec2 offset = drawscale*Vec2(r*cos(t) + currentCircle.y, r*sin(t) + currentCircle.z);
            
            //Determine v1 and v2 in range 0 to maxVelocity
            float v1 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
            float v2 = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/maxVelocity));
            Vec2 velocity = Vec2(v1 - maxVelocity/2, v2 - maxVelocity/2);
            
            this->particles.push_back(CloudParticle(offset, velocity));
        }
    }
}

void ParticleGenerator::Update(GLfloat dt, Vec2 cloud_pos, float particleScale, bool scaleChange){
    //  Update all particles
    for (int i = 0; i < cloudSections.size(); i++){
        int trueAmount = this->amount;
        if (i == 0){trueAmount = 2*this->amount;}
        
        for (GLuint j = 0; j < trueAmount; ++j){
            int index = trueAmount*i + j;
            if (i != 0){index = index + this->amount;}
            
            CloudParticle &p = this->particles[index];
            if(scaleChange){p.lifetime = 0.0f;}
            else{p.lifetime += dt;}
            
            if (p.lifetime <= INIT_TIME){
                p.jostle = Vec2::ZERO;
                float fraction = abs(p.lifetime/INIT_TIME);
                p.position = cloud_pos + fraction*particleScale*p.offset;
                p.color.w = fraction;
            }
            else {
                Vec2 basePosition = cloud_pos + particleScale*p.offset;
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
                p.color.w = 1.0 - abs(greater/maxJostle);
            }
        }
    }
}

void ParticleShader::dispose() {
    Shader::dispose();
    particleTextureWhite.dispose();
    particleTextureGray.dispose();
    particleTextureDarkGray.dispose();
    pg.particles.clear();
    if (master){
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
}

void ParticleShader::onStartup(){
    particleTextureWhite = Texture();
    particleTextureWhite.initWithFile("textures/particle1.png");
    particleTextureGray = Texture();
    particleTextureGray.initWithFile("textures/particle3.png");
    particleTextureDarkGray = Texture();
    particleTextureDarkGray.initWithFile("textures/particle2.png");
    if (master){compileProgram();}
}

// For use by the master CloudNode only
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

// Render all particles for given particleShader
// For use by the master CloudNode only
void ParticleShader::drawParticles(ParticleShader providedPS){
    if (!master){return;}
    CULogGLError();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE);
//    CULogGLError();
//    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//    glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE);
    CULogGLError();
    //reusing the particle shader program
    glUseProgram( _program );
    //    CULog("Program is %d",_program);
    CULogGLError();
    glGenBuffers(1, &VBO);
    CULogGLError();
    glBindBuffer(GL_ARRAY_BUFFER,VBO);
    CULogGLError();

    glBufferData(GL_ARRAY_BUFFER, sizeof(providedPS.particle_quad), providedPS.particle_quad, GL_DYNAMIC_DRAW);
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
    
    if (!rainCloud){
        particleTextureWhite.bind(); CULogGLError();
        _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM ); CULogGLError();
        glUniform1i(_uSprite, TEXTURE_POSITION); CULogGLError();
    }
    
    else{
        particleTextureDarkGray.bind(); CULogGLError();
        _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM ); CULogGLError();
        glUniform1i(_uSprite, TEXTURE_POSITION); CULogGLError();
    }
    
    for (CloudParticle p : providedPS.pg.particles){
        SetVector2f(OFFSET_UNIFORM, providedPS.particleFactor*p.position);
        SetVector3f(ASPECT_UNIFORM, providedPS.aspectRatio);
        SetVector4f(COLOR_UNIFORM, p.color);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0); CULogGLError();
    }
<<<<<<< HEAD
    particleTexture.unbind();
    CULogGLError();
    glDisableVertexAttribArray(_aPosition);
    CULogGLError();
    glDisableVertexAttribArray(_aTexCoords);
    CULogGLError();
    glUseProgram(NULL);
    CULogGLError();

    glDeleteBuffers(1, &EBO);
    glDeleteBuffers(1, &VBO);
=======
    
    if (!rainCloud){particleTextureWhite.unbind(); CULogGLError();}
    else{particleTextureDarkGray.unbind(); CULogGLError();}
    
    glDeleteBuffers(1, &VBO);
    glDeleteBuffers(1, &EBO);
    glDisableVertexAttribArray(_aPosition); CULogGLError();
    glDisableVertexAttribArray(_aTexCoords); CULogGLError();
    glUseProgram(NULL); CULogGLError();
>>>>>>> 1b2ccf02706c055729d55264fdc08b864c22eac0
    // Don't forget to reset to default blending mode
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); CULogGLError();
}

void ParticleShader::update(Vec2 cloud_pos, float dt, float particleScale, bool raining, bool rc){
    //Adjust size of particles based on scale of cloud
    bool scaleChange = false;
    if (_particleScale != particleScale){
        _particleScale = particleScale;
        scaleChange = !raining && !wasRaining;
        for (int i = 0; i < 4; i++){
            particle_quad[i*4] = _particleScale*ogParticleQuad[i*4];
            particle_quad[i*4 + 1] = _particleScale*ogParticleQuad[i*4 + 1];
        }
        
        for(int j = 0; j < pg.cloudSections.size(); j++){
            pg.cloudSections[j].x = ogCloudSections[j].x*_particleScale;
            pg.cloudSections[j].y = ogCloudSections[j].y*_particleScale;
            pg.cloudSections[j].z = ogCloudSections[j].z*_particleScale;
        }
        
        pg.maxJostle = MAX_JOSTLE*_particleScale;
        pg.maxVelocity = MAX_VELOCITY*_particleScale;
    }
    wasRaining = raining;
    rainCloud = rc;
    this->pg.Update(dt, cloud_pos - Vec2(SCENE_WIDTH/2.0f, SCENE_HEIGHT/2.0f), _particleScale, scaleChange);
}
