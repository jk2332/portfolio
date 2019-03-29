//
//  NEWPARTICLESHADER.hpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/28/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef NEWPARTICLESHADER_hpp
#define NEWPARTICLESHADER_hpp

#include <cugl/cugl.h>
#include <stdio.h>
#include "particle.vert"
#include "particle.frag"

//                                  Position        Texcoords
static GLfloat particle_quad[] = {  0.0f,0.0f,      0.0f,0.0f,
                                    0.0f,50.0f,     0.0f,1.0f,
                                    50.0f,0.0f,     1.0f,0.0f,
                                    50.0f,50.0f,    1.0f,1.0f};

static GLuint elements[] = {0, 1, 2, 3, 1, 2};

using namespace cugl;

class newParticleShader : public Shader {
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
    std::shared_ptr<Texture> _mTexture;
    
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
    
    bool first = true;
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
    
    newParticleShader(){}
    
    void onStartup();
    
    void beginShading();
    
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

#endif /* NEWPARTICLESHADER_hpp */
