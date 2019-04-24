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
#include "cloudParticle.vert"
#include "cloudParticle.frag"
#include <vector>
#define MAX_JOSTLE 4.0f
#define MAX_VELOCITY 5.0f
using namespace cugl;

//                                      Position        Texcoords
static GLfloat ogParticleQuad[16]= {-10.0f,-10.0f,   0.0f, 0.0f,
                                    -10.0f, 10.0f,   0.0f, 1.0f,
                                     10.0f,-10.0f,   1.0f, 0.0f,
                                     10.0f, 10.0f,   1.0f, 1.0f};

//                                      Radius CenterX CenterY
static vector<Vec3> ogCloudSections = { Vec3(2.5,   0.0,   1.5), //Central Circle
                                        Vec3(1.3,  -3.8,   1.3),
                                        Vec3(1.3,   3.7,   1.3),
                                        Vec3(0.8,   3.0,   2.7),
                                        Vec3(0.7,   2.5,   0.0),
                                        Vec3(0.8,  -2.9,   2.7),
                                        Vec3(0.6,  -2.6,   0.1),
                                        Vec3(0.8,   1.9,   3.4),
                                        Vec3(0.8,  -2.1,   3.9),
                                        Vec3(0.8,   0.6,   4.0),
                                        Vec3(0.8,  -0.8,   4.7)};

static GLuint elements[] = {0, 1, 2, 3, 1, 2};
static GLuint VAO;
static GLuint VBO;
static GLuint EBO;

// Represents a single particle and its state
struct CloudParticle {
    Vec2 position, velocity, jostle;
    float opacity;
    float life;
    Vec4 color;
    Vec2 offset;
    CloudParticle(Vec2 selectedPosition, Vec2 selectedVelocity) : position(Vec2::ZERO), jostle(Vec2::ZERO), velocity(selectedVelocity), color(Vec4(0.0f,0.0f,0.0f,0.0f)), offset(selectedPosition), opacity(1.0f), life(1.0f){}
};

// ParticleGenerator acts as a container for rendering a large number of
// particles by updating their position based on that of the correpsonding cloud.
class ParticleGenerator {
public:
    // Constructors
    ParticleGenerator();
    ParticleGenerator(GLuint amount);
    // Update all particles
    void Update(GLfloat dt, Vec2 cloud_pos, float particleScale);
    // Render all particles
    void Draw();
    // State
    std::vector<CloudParticle> particles;
    GLuint amount;
    float maxJostle = MAX_JOSTLE;
    float maxVelocity = MAX_VELOCITY;
    //                               Radius CenterX CenterY
    vector<Vec3> cloudSections = {  Vec3(2.5,   0.0,   1.5), //Central Circle
                                    Vec3(1.3,  -3.8,   1.3),
                                    Vec3(1.3,   3.7,   1.3),
                                    Vec3(0.8,   3.0,   2.7),
                                    Vec3(0.7,   2.5,   0.0),
                                    Vec3(0.8,  -2.9,   2.7),
                                    Vec3(0.6,  -2.6,   0.1),
                                    Vec3(0.8,   1.9,   3.4),
                                    Vec3(0.8,  -2.1,   3.9),
                                    Vec3(0.8,   0.6,   4.0),
                                    Vec3(0.8,  -0.8,   4.7)};
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
    //                               Position        Texcoords
    GLfloat particle_quad [16] = {  -10.0f,-10.0f,   0.0f, 0.0f,
                                    -10.0f, 10.0f,   0.0f, 1.0f,
                                     10.0f,-10.0f,   1.0f, 0.0f,
                                     10.0f, 10.0f,   1.0f, 1.0f};
    float _particleScale;
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
    
    ParticleShader(GLuint amount){
        _particleScale = 1.0;
        _pg = ParticleGenerator(amount);
    }
    
    void onStartup();

    void compileProgram();
    
    void drawParticles();
    
    void update(Vec2 cloud_pos, float dt, float particleScale);

    void SetFloat1f(const GLchar *name, float value){
        glUniform1f(glGetUniformLocation(_program, name), value);
    }
    
    void SetVector2f(const GLchar *name, const Vec2 &value){
        glUniform2f(glGetUniformLocation(_program, name), value.x, value.y);
    }
    
    void SetVector4f(const GLchar *name, const Vec4 &value){
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
