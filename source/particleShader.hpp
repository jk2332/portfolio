//
//  particleShader.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/28/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef particleShader_hpp
#define particleShader_hpp

#include <cugl/cugl.h>
#include <stdio.h>
#include "particle.vert"
#include "particle.frag"
#include <vector>
#include "cugl/2d/physics/CUObstacle.h"
using namespace cugl;
////                                  Position      Texcoords
//static GLfloat particle_quad[] = {  0.0f,0.0f,    0.0f,0.0f,
//                                    0.0f,5.0f,    0.0f,1.0f,
//                                    5.0f,0.0f,    1.0f,0.0f,
//                                    5.0f,5.0f,    1.0f,1.0f};

//                                  Position      Texcoords
static GLfloat particle_quad[] = {  -2.5f,-2.5f,    0.0f,0.0f,
                                    -2.5f,2.5f,    0.0f,1.0f,
                                    2.5f,-2.5f,    1.0f,0.0f,
                                    2.5f,2.5f,    1.0f,1.0f};

////                                  Position      Texcoords
//static GLfloat particle_quad[] = {  -5.0f,-5.0f,    0.0f,0.0f,
//                                    -5.0f,5.0f,    0.0f,1.0f,
//                                    5.0f,-5.0f,    1.0f,0.0f,
//                                    5.0f,5.0f,    1.0f,1.0f};

static GLuint elements[] = {0, 1, 2, 3, 1, 2};

static GLuint VAO;
static GLuint VBO;
static GLuint EBO;
//                                  Radius  CenterX CenterY
static Vec3 cloudSections[] = { Vec3(3.9,   -0.5,    -1.0), //Central Half Circle
                                Vec3(2.3,   -1.8,   1.5),
                                Vec3(1.2,   -4.4,   0.3),
                                Vec3(1.2,   3.3,    0.3),
                                Vec3(1.5,   1.8,    1.6),
                                Vec3(1.6,   -0.5,    2.4)};

// Represents a single particle and its state
struct CloudParticle {
    Vec2 position, velocity;
    float opacity;
    float life;
    Vec4 color;
    Vec2 offset;
    CloudParticle() : position(Vec2(0.0f,0.0f)), velocity(Vec2(0.0f,0.0f)), color(Vec4(0.0f,0.0f,0.0f,1.0f)), offset(Vec2(0.0f,0.0f)), opacity(1.0f), life(1.0f){
        
        //Determine circle
        int rand0 = (rand() % 6+1); // in range 1 to 6, inclusive
//        int rand0 = 1;
        Vec3 currentCircle = cloudSections[rand0-1];
//        CULog("rand0 is %d", rand0);
        
        //Determine radius in range 0 to radius of currentCircle
        float r = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX/currentCircle.x));
        float t = 2*M_PI*rand();
        
        offset = 20*Vec2(r*cos(t) + currentCircle.y, r*sin(t) + currentCircle.z);
        
        //If it's the half circle, keep the point in the upper section
        if (rand0 == 1 && offset.y < 0){
            offset.y = -1*offset.y;
        }
        
    }
};

// ParticleGenerator acts as a container for rendering a large number of
// particles by repeatedly spawning and updating particles and killing
// them after a given amount of time.
class ParticleGenerator {
public:
    // Constructors
    ParticleGenerator();
    ParticleGenerator(std::shared_ptr<Obstacle> object, GLuint amount);
    // Update all particles
    void Update(GLfloat dt, GLuint newParticles, Vec2 cloud_pos);
    // Render all particles
    void Draw();
    // State
    std::vector<CloudParticle> particles;
    GLuint amount;
    std::shared_ptr<Obstacle> object;
    // Returns the first Particle index that's currently unused e.g. Life <= 0.0f or 0 if no particle is currently inactive
    int firstUnusedParticle();
    // Respawns particle
    void respawnParticle(CloudParticle &particle, Vec2 offset = Vec2(0.0f, 0.0f));
};

class ParticleShader : public Shader {
protected:
    /** The shader location for the position attribute */
    GLint _aPosition;
    /** The shader location for the texture coordinate attribute */
    GLint _aTexCoords;
    /** The shader location for the projection attribute */
    GLint _uProjection;
    /** The shader location for the offset attribute */
    GLint _uOffset;
    /** The shader location for the color uniform */
    GLint _uColor;
    /** The shader location for the sprite uniform */
    GLint _uSprite;
    /** The current perspective matrix */
    Mat4  _mPerspective;
    /** The current shader texture */
    Texture particleTexture;
    ParticleGenerator _pg;
    /** The OpenGL program for this shader */
    GLuint _program;
    /** The OpenGL vertex shader for this shader */
    GLuint _vertShader;
    /** The OpenGL fragment shader for this shader */
    GLuint _fragShader;
    /** The source string for the vertex shader */
    const char* _vertSource;
    /** The source string for the fragment shader */
    const char* _fragSource;
    GLuint VAO;
    GLuint VBO;
    GLuint EBO;
    
public:
    /**
     * Deletes the OpenGL shader and resets all attributes.
     *
     * You must reinitialize the shader to use it.
     */
    void dispose() override;
   
    ParticleShader(){_pg = ParticleGenerator();}
    
    ParticleShader(std::shared_ptr<Obstacle> object, GLuint amount){
        _pg = ParticleGenerator(object, amount);
    }
    
    void onStartup();

    void compileProgram();
    
    void drawParticles();
    
    void update(Vec2 cloud_pos, float dt, GLuint np);
    
    void SetVector2f(const GLchar *name, const Vec2 &value, GLboolean useShader = false){
        glUniform2f(glGetUniformLocation(_program, name), value.x, value.y);
    }
    
    void SetVector4f(const GLchar *name, const Vec4 &value, GLboolean useShader = false){
        glUniform4f(glGetUniformLocation(_program, name), value.x, value.y, value.z, value.w);
    }
    
    bool validateShader(GLuint shader, const char* type) {
        CUAssertLog( glIsShader( shader ), "Shader %d is not a valid shader", shader);
        GLint shaderCompiled = GL_FALSE;
        glGetShaderiv( shader, GL_COMPILE_STATUS, &shaderCompiled );
        if( shaderCompiled != GL_TRUE ) {
            CULogError( "Unable to compile %s shader %d.\n", type, shader );
            return false;
        }
        return true;
    }
};

#endif /* particleShader_hpp */
