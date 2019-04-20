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
#include "LevelModel.hpp"

using namespace cugl;

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
    _assets->attach<LevelModel>(GenericLoader<LevelModel>::alloc()->getHook());

    // Create a "loading" screen
    _loaded = false;
    _loading.init(_assets);
    
    // Que up the other assets
    AudioChannels::start(24);
    _assets->loadDirectoryAsync("json/assets.json",nullptr);
    _assets->loadAsync<LevelModel>("level1","json/level1.json",nullptr);
    _assets->loadAsync<LevelModel>("level2","json/level2.json",nullptr);
    _assets->loadAsync<LevelModel>("level3","json/level3.json",nullptr);
    _assets->loadAsync<LevelModel>("level4","json/level4.json",nullptr);

    CULogGLError();
    glGenVertexArrays(1, &VAO);
    CULogGLError();
    glBindVertexArray(VAO);
    CULogGLError();
    _batch  = SpriteBatch::alloc();
    CULogGLError();
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
    _levelSelect.dispose();
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
        _levelSelect.init(_assets);
        _loaded = true;
    } else if (!_levelselected && _levelSelect.isActive()){
        _levelSelect.update(0.01f);
    } else if (!_levelselected){
        _levelSelect.dispose();
        CULogGLError();
//        _assets->loadDirectoryAsync("json/assets.json",nullptr);
//        auto x = "json/level" + std::to_string(_levelSelect.getLevelSelected()+1) + ".json";
//        _assets->loadAsync<LevelModel>("foo","json/level" + std::to_string(_levelSelect.getLevelSelected()+1) + ".json",nullptr);
        auto levelId = "level" + std::to_string(_levelSelect.getLevelSelected()+1);
        _gameplay.init(_assets, levelId);
        
        _levelselected = true;
    }else {
        _gameplay.update(timestep);
    }
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
    } else if (!_levelselected){
        //        _gameplay.render(_batch);
        _levelSelect.render(_batch);
    }
    else {
        _gameplay.render(_batch);
    }
}
