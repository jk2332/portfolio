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
    
    Vec2 offset((dimen.width-SCENE_WIDTH)/2.0f,(dimen.height-SCENE_HEIGHT)/2.0f);

    
    // IMMEDIATELY load the splash screen assets
    _assets = assets;
    
    std::shared_ptr<Texture> image = _assets->get<Texture>("bigBackground");
    auto bigBkNode = PolygonNode::allocWithTexture(image);
    bigBkNode->setContentSize(dimen);
    bigBkNode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    addChild(bigBkNode);
    
    image = _assets->get<Texture>("level select background");
    auto bknode = PolygonNode::allocWithTexture(image);
    bknode->setContentSize(SCENE_WIDTH, SCENE_HEIGHT);
    bknode->setAnchor(Vec2::ANCHOR_BOTTOM_LEFT);
    bknode->setPosition(offset);
    addChild(bknode);
    
    auto layer = _assets->get<Node>("levelselect");
    layer->setContentSize(dimen);
    layer->doLayout();
    addChild(layer);
    
    for (int i = 0; i < _num_level; i++){
        std::string name = "levelselect_level" + std::to_string(i);
        auto button = std::dynamic_pointer_cast<Button>(assets->get<Node>(name));
        button->deactivate();
        button->setVisible(false);
        _levelButtons.push_back(button);
        button->setListener([=](const std::string& name, bool down) {
            this->_active = down;
            _selectedLevel = i + 1;
        });
    }
    
    Application::get()->setClearColor(Color4(192,192,192,255));
    return true;
}

static std::vector<std::shared_ptr<cugl::Button> > extracted(const std::vector<std::shared_ptr<cugl::Button> > &_levelButtons) {
    return _levelButtons;
}

/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void LevelSelect::dispose() {
    // Deactivate the button (platform dependent)
    for (int i = 0; i < extracted(_levelButtons).size(); i++){
        if (isPending(i)){
            _levelButtons.at(i)->deactivate();
            _levelButtons.at(i) = nullptr;
        }
    }
    _levelButtons.clear();
    _assets = nullptr;
    removeAllChildren();
//    _selectedLevel = -1;
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
    for (int i = 0; i < _levelButtons.size(); i++){
        _levelButtons.at(i)->setVisible(true);
        _levelButtons.at(i)->activate(i + 1);
//        if (_levelButtons.at(i)->isDown()) {
//            _selectedLevel = i + 1;
//        }
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
