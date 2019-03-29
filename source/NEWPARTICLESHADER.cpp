//
//  NEWPARTICLESHADER.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/28/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "NEWPARTICLESHADER.hpp"

#define POSITION_ATTRIBUTE  "position"
#define TEXCOORDS_ATTRIBUTE  "texCoords"
#define PROJECTION_UNIFORM  "projection"
#define OFFSET_UNIFORM  "offset"
#define COLOR_UNIFORM     "color"
#define SPRITE_UNIFORM     "sprite"
#define TEXTURE_POSITION    0

using namespace cugl;

void newParticleShader::dispose() {
    if (_mTexture != nullptr) { _mTexture.reset(); }
    Shader::dispose();
}

void newParticleShader::onStartup(){
    CULogGLError();
    glGenVertexArrays(1, &this->VAO);
    CULogGLError();
    glBindVertexArray(this->VAO);
    CULogGLError();
    
    glGenBuffers(1, &VBO);
    CULogGLError();
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    CULogGLError();
    glBufferData(GL_ARRAY_BUFFER, sizeof(particle_quad), particle_quad, GL_DYNAMIC_DRAW);
    
    //set up element buffer
    glGenBuffers(1, &EBO);
    CULogGLError();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    CULogGLError();
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(elements), elements, GL_STATIC_DRAW);
    CULogGLError();
}

//do the thing
void newParticleShader::beginShading(){
    if(first) {
        first = false;
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
    CULogGLError();
    // Find each of the attributes
    //        _aPosition = glGetAttribLocation(_program, POSITION_ATTRIBUTE );
    //        if( !validateVariable(_aPosition, POSITION_ATTRIBUTE)) {
    //            dispose();
    //        }
    //        CULogGLError();
    //        _aTexCoords = glGetAttribLocation( _program, TEXCOORDS_ATTRIBUTE );
    //        if( !validateVariable(_aTexCoords, TEXCOORDS_ATTRIBUTE)) {
    //            dispose();
    //        }
    //        CULogGLError();
    //        _uProjection = glGetUniformLocation( _program, PROJECTION_UNIFORM );
    //        if( !validateVariable(_uProjection, PROJECTION_UNIFORM)) {
    //            dispose();
    //        }
    //        CULogGLError();
    //        _uOffset = glGetUniformLocation( _program, OFFSET_UNIFORM );
    //        if( !validateVariable(_uOffset, OFFSET_UNIFORM)) {
    //            dispose();
    //        }
    //        CULogGLError();
    //        _uColor = glGetUniformLocation( _program, COLOR_UNIFORM );
    //        if( !validateVariable(_uColor, COLOR_UNIFORM)) {
    //            dispose();
    //        }
    //        CULogGLError();
    //        _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM );
    //        if( !validateVariable(_uSprite, SPRITE_UNIFORM)) {
    //            dispose();
    //        }
    CULogGLError();
    // Set the texture location and matrix
    //        glEnableVertexAttribArray(_aPosition);
    //        CULogGLError();
    //        glEnableVertexAttribArray(_aTexCoords);
    //        CULogGLError();
    //        if (_mTexture != nullptr) {
    //            glActiveTexture(GL_TEXTURE0 + TEXTURE_POSITION);
    //            CULogGLError();
    //            glBindTexture(GL_TEXTURE_2D, _mTexture->getBuffer());
    //            CULogGLError();
    //        }
    
    //        glUniformMatrix4fv(_uProjection,1,false,_mPerspective.m);
    //        CULogGLError();
    //        glUniform1i(_uSprite, TEXTURE_POSITION);
    //        CULogGLError();
    //        glBindTexture(GL_TEXTURE_2D, 0);
    //        CULogGLError();
    //        glDisableVertexAttribArray(_aPosition);
    //        CULogGLError();
    //        glDisableVertexAttribArray(_aTexCoords);
    //        CULogGLError();
    //
    ////        CUAssertLog(_program, "Shader is not ready for use");
    //        glUseProgram( NULL );
    
    CULogGLError();
    //    shader->bind();
    // Set up mesh and attribute properties
    
    /*GLfloat particle_quad[] = {
     0.0f, 160.0f, 0.0f, 160.0f,
     160.0f, 0.0f, 160.0f, 0.0f,
     0.0f, 0.0f, 0.0f, 0.0f,
     
     0.0f, 160.0f, 0.0f, 160.0f,
     160.0f, 160.0f, 160.0f, 160.0f,
     160.0f, 0.0f, 160.0f, 0.0f
     };
     */
    
    CULogGLError();
    // Set mesh attributes
    
    //        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
    //CULogGLError();
    
    //rebinding presumably because of the spirtebatch that left itself bound
    glBindBuffer(GL_ARRAY_BUFFER,VBO);
    CULogGLError();
    //        glBufferData(GL_ARRAY_BUFFER, sizeof(particle_quad), particle_quad, GL_DYNAMIC_DRAW);
    CULogGLError();
    //reusing the particle shader program
    glUseProgram( _program );
    CULog("Program is %d",_program);
    CULogGLError();
    GLint _aPosition = glGetAttribLocation(_program, POSITION_ATTRIBUTE);
    CULogGLError();
    CULog("Variable address is %d",_aPosition);
    
    glBindVertexArray(this->VAO);
    CULogGLError();
    
    glEnableVertexAttribArray(_aPosition);
    CULogGLError();
    glVertexAttribPointer(_aPosition, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)0);
    CULogGLError();
    
    //        glEnableVertexAttribArray(_aPosition);
    //        CULogGLError();
    //        glEnableVertexAttribArray(_aTexCoords);
    //        CULogGLError();
    //        if (_mTexture != nullptr) {
    //            glActiveTexture(GL_TEXTURE0 + TEXTURE_POSITION);
    //            CULogGLError();
    //            glBindTexture(GL_TEXTURE_2D, _mTexture->getBuffer());
    //            CULogGLError();
    //        }
    
    //SetVector2f(OFFSET_UNIFORM, Vec2(500,500));
    //SetVector4f(COLOR_UNIFORM, Vec4(0.0,0.0,0.0,1.0));
    //
    glActiveTexture(GL_TEXTURE0 + TEXTURE_POSITION);
    
    GLint texAttrib = glGetAttribLocation(_program, TEXCOORDS_ATTRIBUTE);
    CULogGLError();
    glEnableVertexAttribArray(texAttrib);
    CULogGLError();
    glVertexAttribPointer(texAttrib, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (void*)(2 * sizeof(GLfloat)));
    CULogGLError();
    
    Texture particle = Texture();
    particle.initWithFile("textures/particle1.png");
    particle.bind();
    
    _uSprite = glGetUniformLocation( _program, SPRITE_UNIFORM );
    CULogGLError();
    glUniform1i(_uSprite, TEXTURE_POSITION);
    CULogGLError();
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    CULogGLError();
    glBindVertexArray(NULL);
    CULogGLError();
    glUseProgram(NULL);
    CULogGLError();
    // Don't forget to reset to default blending mode
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    CULogGLError();
}
