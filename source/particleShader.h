//
//  particleShader.h
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/13/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef particleShader_h
#define particleShader_h

#include <cugl/renderer/CUShader.h>
#include <cugl/renderer/CUTexture.h>
#include <cugl/math/CUMat4.h>

namespace cugl {
    
    /**
     * This class is a GLSL shader for particles.
     *
     * This class provides you the option to use your own shader sources.  However,
     * any shader used with this class must have the following properties.
     *
     */
    class ParticleShader : public Shader {
#pragma mark Values
    private:
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
        
#pragma mark -
#pragma mark Constructors
    public:
        /**
         * Creates an uninitialized shader with no source.
         *
         * You must initialize the shader to add a source and compiled it.
         */
        ParticleShader() : Shader(), _aPosition(-1), _aTexCoords(-1), _uProjection(-1), _uOffset(-1),
        _uColor(-1), _uSprite(-1) {}
        
        /**
         * Deletes this shader, disposing all resources.
         */
        ~ParticleShader() { dispose(); }
        
        /**
         * Deletes the OpenGL shader and resets all attributes.
         *
         * You must reinitialize the shader to use it.
         */
        void dispose() override;
        
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
        bool init();
        
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
        bool init(std::string vsource, std::string fsource) {
            return init(vsource.c_str(),fsource.c_str());
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
        bool init(const char* vsource, const char* fsource);
        
#pragma mark -
#pragma mark Static Constructors
        /**
         * Returns a new shader with the default vertex and fragment source.
         *
         * The shader will compile the vertex and fragment sources and link
         * them together. When compilation is complete, the shader will not be
         * bound.  However, any shader that was actively bound during compilation
         * also be unbound as well.
         *
         * @return a new shader with the default vertex and fragment source.
         */
        static std::shared_ptr<ParticleShader> alloc() {
            std::shared_ptr<ParticleShader> result = std::make_shared<ParticleShader>();
            return (result->init() ? result : nullptr);
        }
        
        /**
         * Returns a new shader with the given vertex and fragment source.
         *
         * The shader will compile the vertex and fragment sources and link
         * them together. When compilation is complete, the shader will not be
         * bound.  However, any shader that was actively bound during compilation
         * also be unbound as well.
         *
         * @param vsource   The source string for the vertex shader.
         * @param fsource   The source string for the fragment shader.
         *
         * @return a new shader with the given vertex and fragment source.
         */
        static std::shared_ptr<ParticleShader> alloc(std::string vsource, std::string fsource) {
            std::shared_ptr<ParticleShader> result = std::make_shared<ParticleShader>();
            return (result->init(vsource, fsource) ? result : nullptr);
        }
        
        /**
         * Returns a new shader with the given vertex and fragment source.
         *
         * The shader will compile the vertex and fragment sources and link
         * them together. When compilation is complete, the shader will not be
         * bound.  However, any shader that was actively bound during compilation
         * also be unbound as well.
         *
         * @param vsource   The source string for the vertex shader.
         * @param fsource   The source string for the fragment shader.
         *
         * @return a new shader with the given vertex and fragment source.
         */
        static std::shared_ptr<ParticleShader> alloc(const char* vsource, const char* fsource) {
            std::shared_ptr<ParticleShader> result = std::make_shared<ParticleShader>();
            return (result->init(vsource, fsource) ? result : nullptr);
        }
        
#pragma mark -
#pragma mark Attributes
        /**
         * Returns the GLSL location for the position attribute
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the position attribute
         */
        GLint getPositionAttr() const { return _aPosition; }
        
        /**
         * Returns the GLSL location for the textur3e coordinate attribute
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the texture coordinate attribute
         */
        GLint getTexCoordAttr() const { return _aTexCoords; }
        
        /**
         * Returns the GLSL location for the color uniform
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the color uniform
         */
        GLint getColorUni() const { return _uColor; }
        
        /**
         * Returns the GLSL location for the projection uniform
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the projection uniform
         */
        GLint getProjectionUni() const { return _uProjection; }
        
        /**
         * Returns the GLSL location for the offset uniform
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the offset uniform
         */
        GLint getOffsetUni() const { return _uOffset; }
        
        /**
         * Returns the GLSL location for the sprite uniform
         *
         * This method will return -1 if the program is not initialized.
         *
         * @return the GLSL location for the sprite uniform
         */
        GLint getSpriteUni() const { return _uSprite; }
        
        /**
         * Sets the perspective matrix to use in the shader.
         *
         * @param matrix    The perspective matrix
         */
        void setPerspective(const Mat4&  matrix);
        
        /**
         * Returns the current perspective matrix in use.
         *
         * @return the current perspective matrix in use.
         */
        const Mat4& getPerspective() { return _mPerspective; }
        
        /**
         * Sets the texture in use in the shader
         *
         * @param texture   The shader texture
         */
        void setTexture(const std::shared_ptr<Texture>& texture);
        
        /**
         * Returns the current texture in use.
         *
         * @return the current texture in use.
         */
        std::shared_ptr<Texture> getTexture() { return _mTexture; }
        
        /**
         * Returns the current texture in use.
         *
         * @return the current texture in use.
         */
        const std::shared_ptr<Texture>& getTexture() const { return _mTexture; }
        
        //additional functions from learnopengl.com
        void SetFloat(const GLchar *name, GLfloat value, GLboolean useShader = false);
        
        void SetInteger(const GLchar *name, GLint value, GLboolean useShader = false);
        
        void SetVector2f(const GLchar *name, GLfloat x, GLfloat y, GLboolean useShader = false);
        
        void SetVector2f(const GLchar *name, const Vec2 &value, GLboolean useShader = false);
        
        void SetVector3f(const GLchar *name, GLfloat x, GLfloat y, GLfloat z, GLboolean useShader = false);
        
        void SetVector3f(const GLchar *name, const Vec3 &value, GLboolean useShader = false);
        
        void SetVector4f(const GLchar *name, GLfloat x, GLfloat y, GLfloat z, GLfloat w, GLboolean useShader = false);
        
        void SetVector4f(const GLchar *name, const Vec4 &value, GLboolean useShader = false);
        
//        void SetMatrix4(const GLchar *name, const Mat4 &matrix, GLboolean useShader = false);
        
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
        void attach(GLuint vArray, GLuint vBuffer);
        
        /**
         * Binds this shader, making it active.
         *
         * Once bound, any OpenGL calls will then be sent to this shader.
         */
        void bind() override;
        
        /**
         * Unbinds this shader, making it no longer active.
         *
         * Once unbound, OpenGL calls will no longer be sent to this shader.
         */
        void unbind() override;
        
#pragma mark -
#pragma mark Compilation
    protected:
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
        bool compile() override;
        
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
        bool validateVariable(GLint variable, const char* name);
        
    };
    
}

#endif /* particleShader_h */
