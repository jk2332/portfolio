//
//  LevelSelect.cpp
//  WeatherDefender
//
//  Created by 김지원 on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "LevelSelect.hpp"

using namespace cugl;

/** The ID for the button listener */
#define LISTENER_ID 1

/** This is adjusted by screen aspect ratio to get the height */
#define SCENE_WIDTH 1024
#define SCENE_HEIGHT 576
float BUTTON_X[] = { 0.f, 350.f, 200, 250};
float BUTTON_Y[] = { 150.f, 200.f, 150, 300};

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
bool LevelSelect::init(const std::shared_ptr<cugl::AssetManager>& assets) {
    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    Size dimen = computeActiveSize();
    if (assets == nullptr) {
        return false;
    } else if (!Scene::init(dimen)) {
        return false;
    }
    _active=true;
    
    // IMMEDIATELY load the splash screen assets
    _assets = assets;
    _assets->loadDirectory("json/levelselect.json");
    std::shared_ptr<Texture> image = _assets->get<Texture>("level select background");
    auto bknode = PolygonNode::allocWithTexture(image);
    bknode->setContentSize(dimen);
    addChild(bknode);
    
    //    _bar = std::dynamic_pointer_cast<ProgressBar>(assets->get<Node>("load_bar"));
    for (int i = 0; i < _num_level; i++){
        std::string name = "level" + std::to_string(i) + "button";
        auto button_node = PolygonNode::allocWithTexture(_assets->get<Texture>(name));
        button_node->setContentSize(dimen/10);
        button_node->setPosition(BUTTON_X[i], BUTTON_Y[i]);
        std::shared_ptr<Button> button = Button::alloc(button_node);
        button->setPosition(BUTTON_X[i], BUTTON_Y[i]);
        button->setListener([=](const std::string& name, bool down) {
            if (this->_active){
                this->_active = down;
            }
        });
        button->setContentSize(dimen/10);
        button->setVisible(true);
        button->activate(i + 1);
        _levelButtons.push_back(button);
        addChild(button);
    }
    Application::get()->setClearColor(Color4(192,192,192,255));
    return true;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void LevelSelect::dispose() {
    // Deactivate the button (platform dependent)
    for (int i = 0; i < _levelButtons.size(); i++){
        if (isPending(i)){
            _levelButtons.at(i)->deactivate();
            //            _levelButtons.at(i)->removeListener();
        }
    }
    _levelButtons.clear();
    _assets = nullptr;
    _selectedLevel = -1;
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
void LevelSelect::update(float progress) {
    //    if (_progress < 1) {
    //        _progress = _assets->progress();
    //        if (_progress >= 1) {
    //            _progress = 1.0f;
    //            _button->setVisible(true);
    //            _button->activate(1);
    //        }
    //        _bar->setProgress(_progress);
    //    }
    //    _selectedLevel = -1;
    //    _selected = false;
    for (int i =0; i < _levelButtons.size(); i++){
        _levelButtons.at(i)->setVisible(true);
        _levelButtons.at(i)->activate(i);
        if (_levelButtons.at(i)->isDown()) {
            CULog("selected level: %i", i);
            _selectedLevel = i;
        }
    }
}

/**
 * Returns true if loading is complete, but the player has not pressed play
 *
 * @return true if loading is complete, but the player has not pressed play
 */
bool LevelSelect::isPending(int i) const {
    return _levelButtons.at(i) != nullptr && _levelButtons.at(i)->isVisible();
    
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size LevelSelect::computeActiveSize() const {
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
