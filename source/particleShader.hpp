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
#include "Constants.hpp"

using namespace cugl;

//                                      Radius CenterX CenterY
const vector<Vec3> ogCloudSections = {  Vec3(1.25,  0.0,   0.0), //Central Circle
                                        Vec3(0.65, -1.9,  -0.1),
                                        Vec3(0.65,  1.85, -0.1),
                                        Vec3(0.40,  1.5,   0.6),
                                        Vec3(0.35,  1.25, -0.75),
                                        Vec3(0.40, -1.45,  0.6),
                                        Vec3(0.30, -1.3,  -0.7),
                                        Vec3(0.40,  0.95,  0.95),
                                        Vec3(0.40, -1.05,  1.2),
                                        Vec3(0.40,  0.3,   1.25),
                                        Vec3(0.40, -0.4,   1.6)};

static GLuint elements[] = {0, 1, 2, 3, 1, 2};
static GLuint VAO;
static GLuint VBO;
static GLuint EBO;

// Represents a single particle and its state
struct CloudParticle {
    Vec2 position, velocity, jostle, offset;
    Vec4 color;
    float lifetime;
    CloudParticle(Vec2 selectedPosition, Vec2 selectedVelocity) : position(Vec2::ZERO), jostle(Vec2::ZERO), velocity(selectedVelocity), color(Vec4::ZERO), lifetime(0.0f), offset(selectedPosition){}
};

// ParticleGenerator acts as a container for rendering a large number of
// particles by updating their position based on that of the correpsonding cloud.
class ParticleGenerator {
public:
    // Constructors
    ParticleGenerator();
    ParticleGenerator(GLuint amount, float ds);
    // Update all particles
    void Update(GLfloat dt, Vec2 cloud_pos, float particleScale, bool scaleChange);
    // Render all particles
    void Draw();
    // State
    std::vector<CloudParticle> particles;
    GLuint amount;
    float drawscale = 1.0f;
    float maxJostle = MAX_JOSTLE;
    float maxVelocity = MAX_VELOCITY;
    //                              Radius CenterX CenterY
    vector<Vec3> cloudSections = {  Vec3(1.25,  0.0,   0.0), //Central Circle
                                    Vec3(0.65, -1.9,  -0.1),
                                    Vec3(0.65,  1.85, -0.1),
                                    Vec3(0.40,  1.5,   0.6),
                                    Vec3(0.35,  1.25, -0.75),
                                    Vec3(0.40, -1.45,  0.6),
                                    Vec3(0.30, -1.3,  -0.7),
                                    Vec3(0.40,  0.95,  0.95),
                                    Vec3(0.40, -1.05,  1.2),
                                    Vec3(0.40,  0.3,   1.25),
                                    Vec3(0.40, -0.4,   1.6)};
};

class ParticleShader : public Shader {
public:
    ParticleGenerator pg;
protected:
    /** Is this shader the master particleShader?*/
    bool master;
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
    /** The default shader texture */
    Texture particleTextureWhite;
    /** The darker shader texture */
    Texture particleTextureGray;
    /** The darkest shader texture */
    Texture particleTextureDarkGray;
    float particleFactor;
    //                              Position     Texcoords
    GLfloat ogParticleQuad[16] = {  0.0f, 0.0f,  0.0f, 0.0f,
                                    0.0f, 0.0f,  0.0f, 1.0f,
                                    0.0f, 0.0f,  1.0f, 0.0f,
                                    0.0f, 0.0f,  1.0f, 1.0f};
    //                              Position     Texcoords
    GLfloat particle_quad [16] = {  0.0f,0.0f,   0.0f, 0.0f,
                                    0.0f,0.0f,   0.0f, 1.0f,
                                    0.0f,0.0f,   1.0f, 0.0f,
                                    0.0f,0.0f,   1.0f, 1.0f};
    float _particleScale;
    Vec3 aspectRatio;
    //was it raining in the previous update loop?
    bool wasRaining;
    //is this cloud large enough to be a raincloud?
    bool rainCloud;
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
   
    ParticleShader(){pg = ParticleGenerator();}
    
    ParticleShader(GLuint amount, float ds, Vec3 ar, GLfloat pq[16], float pf, bool m){
        master = m;
        _particleScale = 0.0f;
        pg = ParticleGenerator(amount, ds);
        aspectRatio = ar;
        particleFactor = pf;
        wasRaining = false;
        rainCloud = false;
        for(int i = 0; i < 16; i++){
            ogParticleQuad[i] = pq[i];
            particle_quad[i] = pq[i];
        }
    }
    
    void onStartup();

    void compileProgram();
    
    void drawParticles(ParticleShader providedPS);
    
    void update(Vec2 cloud_pos, float dt, float particleScale, bool raining, bool rc);

    void SetFloat1f(const GLchar *name, float value){
        glUniform1f(glGetUniformLocation(_program, name), value);
    }
    
    void SetVector2f(const GLchar *name, const Vec2 &value){
        glUniform2f(glGetUniformLocation(_program, name), value.x, value.y);
    }

    void SetVector3f(const GLchar *name, const Vec3 &value){
        glUniform3f(glGetUniformLocation(_program, name), value.x, value.y, value.z);
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
