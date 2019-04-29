//
//  LevelSelect.cpp
//  WeatherDefender
//
//  Created by 김지원 on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "PauseMenu.hpp"

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
bool PauseMenu::init(const std::shared_ptr<cugl::AssetManager>& assets) {
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
    if (!_assetLoaded){
        _assets->loadDirectory("json/pausemenu.json");
        _backToLevelButton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pauseMenu_plevelselectbutton"));
        _backToLevelButton->setListener([=](const std::string& name, bool down) {
            this->_active = down;
        });
        _backToLevelButton->activate(100);
        for (auto &c : getChildren()){
            std::cout << c->getName() << endl;
        }
        addChild(_backToLevelButton);
        
        _quitButton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pauseMenu_pquitbutton"));
        _quitButton->setListener([=](const std::string& name, bool down) {
            this->_active = down;
            Application::get()->quit();
        });
        _quitButton->activate(101);
        addChild(_quitButton);
        
        _continueButton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("pauseMenu_pcontinuebutton"));
        _continueButton->setListener([=](const std::string& name, bool down) {
            this->_active = down;
        });
        _continueButton->activate(102);
        addChild(_continueButton);
    }

    
    _continueSelected = false;
    _backToLevelSelected = false;
    
    
//    Application::get()->setClearColor(Color4(192,192,192,255));
    Application::get()->onSuspend();
    return true;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void PauseMenu::dispose() {
    // Deactivate the button (platform dependent)
//    if (isPending(_backToMainButton))_backToMainButton->deactivate();
    if (isPending(_backToLevelButton)) _backToLevelButton->deactivate();
    if (isPending(_quitButton)) _quitButton->deactivate();
    if (isPending(_continueButton)) _continueButton->deactivate();
    _backToLevelButton = nullptr;
//    _backToMainButton = nullptr;
    _continueButton = nullptr;
    _quitButton = nullptr;
    _assets = nullptr;
//    _backToMainSelected = false;
    _continueSelected = false;
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
void PauseMenu::update(float progress) {
    if (_backToLevelButton->isDown()){
        _backToLevelSelected= true;
    }
    else if (_continueButton->isDown()){
        _continueSelected = true;
    }
    
}

/**
 * Returns true if loading is complete, but the player has not pressed play
 *
 * @return true if loading is complete, but the player has not pressed play
 */
bool PauseMenu::isPending(std::shared_ptr<Button> b) const {
    return b != nullptr && b->isVisible();
    
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size PauseMenu::computeActiveSize() const {
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


