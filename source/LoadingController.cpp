//
//  LoadingController.h
//  Weather Defender
//
//  This module provides a very barebones loading screen.  Most of the time you
//  will not need a loading screen, because the assets will load so fast.  But
//  just in case, this is a simple example you can use in your games.
//
//  We know from 3152 that you all like to customize this screen.  Therefore,
//  we have kept it as simple as possible so that it is easy to modify.
//
//  Author: Walker White
//  Version: 1/10/17
//
#include "LoadingController.h"

using namespace cugl;

/** The ID for the button listener */
#define LISTENER_ID 1

/** This is adjusted by screen aspect ratio to get the height */
#define SCENE_WIDTH 1024
#define SCENE_HEIGHT 576

#pragma mark -
#pragma mark Constructors

/**
 * Initializes the controller contents, making it ready for loading
 *
 * The constructor does not allocate any objects or memory.  This allows
 * us to have a non-pointer reference to this controller, reducing our
 * memory allocation.  Instead, allocation happens in this method.
 *
 * @param assets    The (loaded) assets for this game mode
 *
 * @return true if the controller is initialized properly, false otherwise.
 */
bool LoadingScene::init(const std::shared_ptr<cugl::AssetManager>& assets) {
    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    Size dimen = computeActiveSize();
    if (assets == nullptr) {
        return false;
    } else if (!Scene::init(dimen)) {
        return false;
    }
    
    _active = true;
    // IMMEDIATELY load the splash screen assets
    _assets = assets;
    _assets->loadDirectory("json/loading.json");
    _assets->loadDirectory("json/levelselect.json");
    
    _assets->loadAsync<LevelModel>("level1","json/level1.json",nullptr);
    _assets->loadAsync<LevelModel>("level2","json/level2.json",nullptr);
    _assets->loadAsync<LevelModel>("level3","json/level3.json",nullptr);
    _assets->loadAsync<LevelModel>("level4","json/level4.json",nullptr);
    
    auto layer = assets->get<Node>("load");
    layer->setContentSize(dimen);
    layer->doLayout(); // This rearranges the children to fit the screen
    addChild(layer);
    
    _bar = std::dynamic_pointer_cast<ProgressBar>(assets->get<Node>("load_bar"));
    _button = std::dynamic_pointer_cast<Button>(assets->get<Node>("load_claw_play"));
    _button->deactivate();
    _button->setVisible(false);
    _button->setListener([=](const std::string& name, bool down) {
        this->_active = down;
    });
    Application::get()->setClearColor(Color4(192,192,192,255));

    return true;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void LoadingScene::dispose() {
    // Deactivate the button (platform dependent)
    if (isPending()) {
        _button->deactivate();
    }
    _button = nullptr;
    _bar = nullptr;
    _assets = nullptr;
    _progress = 0.0f;
    removeAllChildren();
}


#pragma mark -
#pragma mark Progress Monitoring
/**
 * The method called to update the game mode.
 *
 * This method updates the progress bar amount.
 *
 * @param timestep  The amount of time (in seconds) since the last frame
 */
void LoadingScene::update(float progress) {
    if (_progress < 1) {
        _progress = _assets->progress();
        if (_progress >= 1) {
            _progress = 1.0f;
            _button->setVisible(true);
            _button->activate(0);
        }
        _bar->setProgress(_progress);
    }
}

/**
 * Returns true if loading is complete, but the player has not pressed play
 *
 * @return true if loading is complete, but the player has not pressed play
 */
bool LoadingScene::isPending( ) const {
    return _button != nullptr && _button->isVisible();
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size LoadingScene::computeActiveSize() const {
    Size dimen = Application::get()->getDisplaySize();
    float ratio1 = dimen.width/dimen.height;
    float ratio2 = ((float)SCENE_WIDTH)/((float)SCENE_HEIGHT);
    if (ratio1 < ratio2) {
        dimen *= SCENE_WIDTH/dimen.width;
    } else {
        dimen *= SCENE_HEIGHT/dimen.height;
    }
    return dimen;
}
