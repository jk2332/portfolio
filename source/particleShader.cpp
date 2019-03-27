//
//  particleShader.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/13/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include <stdio.h>
#include "particleShader.h"
#include <cugl/renderer/CUSpriteShader.h>
#include <cugl/renderer/CUVertex.h>
#include <cugl/util/CUDebug.h>

// The shaders
//#include "particle.vert"
//#include "particle.frag"

// The names of the shader attributes and uniforms
#define POSITION_ATTRIBUTE  "position"
#define TEXCOORDS_ATTRIBUTE  "texCoords"
#define PROJECTION_UNIFORM  "projection"
#define OFFSET_UNIFORM  "offset"
#define COLOR_UNIFORM     "color"
#define SPRITE_UNIFORM     "sprite"
#define TEXTURE_POSITION    0

using namespace cugl;


#pragma mark -
#pragma mark Initialization
/**
 * Initializes this shader with the default vertex and fragment source.
 *
 * The shader will compile the vertex and fragment sources and link
 * them together. When compilation is complete, the shader will not be
 * bound.  However, any shader that was actively bound during compilation
 * also be unbound as well.
 *
 * @return true if initialization was successful.
 */
bool ParticleShader::init() {
//    _vertSource = oglCTVert;
//    _fragSource = oglCTFrag;
    CULogGLError();
    bool b = compile();
    CULogGLError();
    return b;
}
/**
 * Initializes this shader with the given vertex and fragment source.
 *
 * The shader will compile the vertex and fragment sources and link
 * them together. When compilation is complete, the shader will not be
 * bound.  However, any shader that was actively bound during compilation
 * also be unbound as well.
 *
 * @param vsource   The source string for the vertex shader.
 * @param fsource   The source string for the fragment shader.
 *
 * @return true if initialization was successful.
 */
bool ParticleShader::init(const char* vsource, const char* fsource) {
    _vertSource = vsource;
    _fragSource = fsource;
    return compile();
}

#pragma mark -
#pragma mark Attributes
/**
 * Sets the perspective matrix to use in the shader.
 *
 * @param matrix    The perspective matrix
 */
void ParticleShader::setPerspective(const Mat4& matrix) {
    _mPerspective = matrix;
    if (_active) {
        glUniformMatrix4fv(_uProjection,1,false,_mPerspective.m);
    }
}

/**
 * Sets the texture in use in the shader
 *
 * @param texture   The shader texture
 */
void ParticleShader::setTexture(const std::shared_ptr<Texture>& texture) {
    _mTexture = texture;
    if (_active) {
        glActiveTexture(GL_TEXTURE0 + TEXTURE_POSITION);
        glBindTexture(GL_TEXTURE_2D, _mTexture->getBuffer());
    }
}

#pragma mark -
#pragma mark Rendering

/**
 * Attaches the given memory buffer to this shader.
 *
 * Because of limitations in OpenGL ES, we cannot draw anything without
 * both a vertex buffer object and an vertex array object.
 *
 * @param vArray    The vertex array object
 * @param vBuffer   The vertex buffer object
 */
void ParticleShader::attach(GLuint vArray, GLuint vBuffer) {
    CUAssertLog(_active, "This shader is not currently active");
    
    glBindVertexArray(vArray);
    glBindBuffer(GL_ARRAY_BUFFER, vBuffer);
    
    glVertexAttribPointer( _aPosition, 2, GL_FLOAT, GL_FALSE, sizeof(Vertex2),
                          Vertex2::positionOffset());
    glVertexAttribPointer( _aTexCoords, 2, GL_FLOAT, GL_FALSE, sizeof(Vertex2),
                          Vertex2::texcoordOffset());
}

/**
 * Binds this shader, making it active.
 *
 * Once bound, any OpenGL calls will then be sent to this shader.
 */
void ParticleShader::bind() {
    Shader::bind();
    glEnableVertexAttribArray(_aPosition);
    CULogGLError();
    glEnableVertexAttribArray(_aTexCoords);
    CULogGLError();
    if (_mTexture != nullptr) {
        glActiveTexture(GL_TEXTURE0 + TEXTURE_POSITION);
        CULogGLError();
        glBindTexture(GL_TEXTURE_2D, _mTexture->getBuffer());
        CULogGLError();
    }
}

/**
 * Unbinds this shader, making it no longer active.
 *
 * Once unbound, OpenGL calls will no longer be sent to this shader.
 */
void ParticleShader::unbind() {
    glBindTexture(GL_TEXTURE_2D, 0);
    CULogGLError();
    glDisableVertexAttribArray(_aPosition);
    CULogGLError();
    glDisableVertexAttribArray(_aTexCoords);
    CULogGLError();
    Shader::unbind();
    CULogGLError();
}

#pragma mark -
#pragma mark Compilation
/**
 * Compiles this shader from the given vertex and fragment shader sources.
 *
 * When compilation is complete, the shader will not be bound.  However,
 * any shader that was actively bound during compilation also be unbound
 * as well.
 *
 * If compilation fails, it will display error messages on the log.
 *
 * @return true if compilation was successful.
 */
bool ParticleShader::compile() {
    CULogGLError();
    if (!Shader::compile()) return false;
    CULogGLError();
    // Find each of the attributes
    _aPosition = glGetAttribLocation( _program, POSITION_ATTRIBUTE );
    if( !validateVariable(_aPosition, POSITION_ATTRIBUTE)) {
        dispose();
        return false;
    }
    
    _aTexCoords = glGetAttribLocation( _program, TEXCOORDS_ATTRIBUTE );
    if( !validateVariable(_aTexCoords, TEXCOORDS_ATTRIBUTE)) {
        dispose();
        return false;
    }
    
    _uProjection = glGetUniformLocation( _program, PROJECTION_UNIFORM );
    if( !validateVariable(_uProjection, PROJECTION_UNIFORM)) {
        dispose();
        return false;
    }
    
    _uOffset = glGetUniformLocation( _program, OFFSET_UNIFORM );
    if( !validateVariable(_uOffset, OFFSET_UNIFORM)) {
        dispose();
        return false;
    }
    
    _uColor = glGetUniformLocation( _program, COLOR_UNIFORM );
    if( !validateVariable(_uColor, COLOR_UNIFORM)) {
        dispose();
        return false;
    }
    
    _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM );
    if( !validateVariable(_uSprite, SPRITE_UNIFORM)) {
        dispose();
        return false;
    }
    
    // Set the texture location and matrix
    bind();
    glUniformMatrix4fv(_uProjection,1,false,_mPerspective.m);
    glUniform1i(_uSprite, TEXTURE_POSITION);
    unbind();
    
    return true;
}

/**
 * Returns true if the GLSL variable was found in this shader.
 *
 * If variable is not found, it will display error messages on the log.
 *
 * @param variable  The variable (reference) to test
 * @param name      The variable (name) to test
 *
 * @return true if the GLSL variable was found in this shader.
 */
bool ParticleShader::validateVariable(GLint variable, const char* name) {
    if( variable == -1 ) {
        CULogError( "%s is not a valid GLSL program variable.\n", name );
        Shader::logProgramError(_program);
        return false;
    }
    return true;
}

/**
 * Deletes the OpenGL shader and resets all attributes.
 *
 * You must reinitialize the shader to use it.
 */
void ParticleShader::dispose() {
    if (_mTexture != nullptr) { _mTexture.reset(); }
    Shader::dispose();
}

void ParticleShader::SetFloat(const GLchar *name, GLfloat value, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform1f(glGetUniformLocation(_program, name), value);
}
void ParticleShader::SetInteger(const GLchar *name, GLint value, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform1i(glGetUniformLocation(_program, name), value);
}
void ParticleShader::SetVector2f(const GLchar *name, GLfloat x, GLfloat y, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform2f(glGetUniformLocation(_program, name), x, y);
}
void ParticleShader::SetVector2f(const GLchar *name, const Vec2 &value, GLboolean useShader){
    if (useShader)
    this->bind();
    glGetError();
    GLuint x = glGetUniformLocation(_program, name);
    if (glGetError() != GL_NO_ERROR) {
        GLint id;
        glGetIntegerv(GL_CURRENT_PROGRAM,&id);
        CULog("Program is %d vs %d",_program,id);
        CULog("%s at %d",name,x);
        CULogGLError();
        CUAssert(false);
    }
    glUniform2f(glGetUniformLocation(_program, name), value.x, value.y);
}
void ParticleShader::SetVector3f(const GLchar *name, GLfloat x, GLfloat y, GLfloat z, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform3f(glGetUniformLocation(_program, name), x, y, z);
}
void ParticleShader::SetVector3f(const GLchar *name, const Vec3 &value, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform3f(glGetUniformLocation(_program, name), value.x, value.y, value.z);
    CULogGLError();
}
void ParticleShader::SetVector4f(const GLchar *name, GLfloat x, GLfloat y, GLfloat z, GLfloat w, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform4f(glGetUniformLocation(_program, name), x, y, z, w);
}
void ParticleShader::SetVector4f(const GLchar *name, const Vec4 &value, GLboolean useShader){
    if (useShader)
    this->bind();
    glUniform4f(glGetUniformLocation(_program, name), value.x, value.y, value.z, value.w);
    
}
//void ParticleShader::SetMatrix4(const GLchar *name, const Mat4 &matrix, GLboolean useShader){
//    if (useShader)
//    this->bind();
//    glUniformMatrix4fv(glGetUniformLocation(_program, name), 1, GL_FALSE, glm::value_ptr(matrix));
//}
