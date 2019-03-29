//
//  WDApp.h
//  Weather Defender
//  This is the root class for your game.  The file main.cpp accesses this class
//  to run the application.  While you could put most of your game logic in
//  this class, we prefer to break the game up into player modes and have a
//  class for each mode.
//
//  This file is based on the CS 3152 PhysicsDemo Lab by Don Holden, 2007
//
//  Author: Walker White and Anthony Perello
//  Version: 1/26/17
//
#include "WDApp.h"
//                                  Position        Texcoords
static GLfloat particle_quad[] = {  0.0f,0.0f,      0.0f,0.0f,
                                    0.0f,50.0f,     0.0f,1.0f,
                                    50.0f,0.0f,     1.0f,0.0f,
                                    50.0f,50.0f,    1.0f,1.0f
};

static GLuint elements[] = {0, 1, 2, 3, 1, 2};

static GLuint VBO;

static GLuint EBO;

using namespace cugl;
// The shaders
#include "particle.vert"
#include "particle.frag"
#define POSITION_ATTRIBUTE  "position"
#define TEXCOORDS_ATTRIBUTE  "texCoords"
#define PROJECTION_UNIFORM  "projection"
#define OFFSET_UNIFORM  "offset"
#define COLOR_UNIFORM     "color"
#define SPRITE_UNIFORM     "sprite"
#define TEXTURE_POSITION    0

#pragma mark -
#pragma mark Application State

/**
 * The method called after OpenGL is initialized, but before running the application.
 *
 * This is the method in which all user-defined program intialization should
 * take place.  You should not create a new init() method.
 *
 * When overriding this method, you should call the parent method as the
 * very last line.  This ensures that the state will transition to FOREGROUND,
 * causing the application to run.
 */
void WeatherDefenderApp::onStartup() {
    CULogGLError();
    _assets = AssetManager::alloc();
    CULogGLError();
    // Start-up basic input
#ifdef CU_TOUCH_SCREEN
    Input::activate<Touchscreen>();
#else
    Input::activate<Mouse>();
#endif
    
    _assets->attach<Font>(FontLoader::alloc()->getHook());
    _assets->attach<Texture>(TextureLoader::alloc()->getHook());
    _assets->attach<Sound>(SoundLoader::alloc()->getHook());
    _assets->attach<Node>(SceneLoader::alloc()->getHook());

    // Create a "loading" screen
    _loaded = false;
    _loading.init(_assets);
    
    // Que up the other assets
    AudioChannels::start(24);
    _assets->loadDirectoryAsync("json/assets.json",nullptr);
    
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
    
    _batch  = SpriteBatch::alloc();
    
    Application::onStartup(); // YOU MUST END with call to parent
}

/**
 * The method called when the application is ready to quit.
 *
 * This is the method to dispose of all resources allocated by this
 * application.  As a rule of thumb, everything created in onStartup()
 * should be deleted here.
 *
 * When overriding this method, you should call the parent method as the
 * very last line.  This ensures that the state will transition to NONE,
 * causing the application to be deleted.
 */
void WeatherDefenderApp::onShutdown() {
    _loading.dispose();
    _gameplay.dispose();
    _assets = nullptr;
    _batch = nullptr;
    
    // Shutdown input
#ifdef CU_TOUCH_SCREEN
    Input::deactivate<Touchscreen>();
#else
    Input::deactivate<Mouse>();
#endif
    
    AudioChannels::stop();
    Application::onShutdown();  // YOU MUST END with call to parent
}

/**
 * The method called when the application is suspended and put in the background.
 *
 * When this method is called, you should store any state that you do not
 * want to be lost.  There is no guarantee that an application will return
 * from the background; it may be terminated instead.
 *
 * If you are using audio, it is critical that you pause it on suspension.
 * Otherwise, the audio thread may persist while the application is in
 * the background.
 */
void WeatherDefenderApp::onSuspend() {
    AudioChannels::get()->pauseAll();
}

/**
 * The method called when the application resumes and put in the foreground.
 *
 * If you saved any state before going into the background, now is the time
 * to restore it. This guarantees that the application looks the same as
 * when it was suspended.
 *
 * If you are using audio, you should use this method to resume any audio
 * paused before app suspension.
 */
void WeatherDefenderApp::onResume() {
    AudioChannels::get()->resumeAll();
}


#pragma mark -
#pragma mark Application Loop

/**
 * The method called to update the application data.
 *
 * This is your core loop and should be replaced with your custom implementation.
 * This method should contain any code that is not an OpenGL call.
 *
 * When overriding this method, you do not need to call the parent method
 * at all. The default implmentation does nothing.
 *
 * @param timestep  The amount of time (in seconds) since the last frame
 */
void WeatherDefenderApp::update(float timestep) {
    if (!_loaded && _loading.isActive()) {
        _loading.update(0.01f);
    } else if (!_loaded) {
        _loading.dispose(); // Disables the input listeners in this mode
        CULogGLError();
        _gameplay.init(_assets);
        _loaded = true;
    } else {
        _gameplay.update(timestep);
    }
}

bool validateVariable(GLint variable, const char* name) {
    if( variable == -1 ) {
        CULogError( "%s is not a valid GLSL program variable.\n", name );
        return false;
    }
    return true;
}

/**
 * The method called to draw the application to the screen.
 *
 * This is your core loop and should be replaced with your custom implementation.
 * This method should OpenGL and related drawing calls.
 *
 * When overriding this method, you do not need to call the parent method
 * at all. The default implmentation does nothing.
 */
void WeatherDefenderApp::draw() {
    if (!_loaded) {
        _loading.render(_batch);
    } else {
//        _gameplay.render(_batch);
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
}

