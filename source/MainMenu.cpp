//
//  LevelSelect.cpp
//  WeatherDefender
//
//  Created by 김지원 on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "MainMenu.hpp"

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
bool MainMenu::init(const std::shared_ptr<cugl::AssetManager>& assets) {
    // Initialize the scene to a locked height (iPhone X is narrow, but wide)
    _dimen = computeActiveSize();
    if (assets == nullptr) {
        return false;
    } else if (!Scene::init(_dimen)) {
        return false;
    }
    _active=true;
    
    // IMMEDIATELY load the splash screen assets
    _assets = assets;
    ticks = 01;
    buttoncooldown = 01;
    
    std::shared_ptr<Texture> image = _assets->get<Texture>("main menu background");
    auto bknode = PolygonNode::allocWithTexture(image);
    bknode->setContentSize(_dimen);
    addChild(bknode);
    
    auto layer = _assets->get<Node>("main");
    layer->setContentSize(_dimen);
    layer->doLayout();
    
    _startbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_start"));
    _startbutton->deactivate();
    _startbutton->setVisible(false);
    _startbutton->setListener([=](const std::string& name, bool down) {
        this->_active = down;
        _startSelected = true;
    });
    
    _levelbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_level select"));
    _levelbutton->deactivate();
    _levelbutton->setVisible(false);
    _levelbutton->setListener([=](const std::string& name, bool down) {
        CULog("level button clicked");
        this->_active = down;
        _levelSelected = true;
    });
    
    _instbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_instructions"));
    _instbutton->deactivate();
    _instbutton->setVisible(false);
    _instbutton->setListener([=](const std::string& name, bool down) {
//        this->_active = down;
        _instSelected = true;
    });
    
    _creditbutton = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_credit"));
    _creditbutton->deactivate();
    _creditbutton->setVisible(false);
    _creditbutton->setListener([=](const std::string& name, bool down) {
//        this->_active = down;
        _creditSelected = true;
    });
    
    _creditboard = std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_creditboard"));
    _creditboard->setVisible(false);
    
    _creditback = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_creditboard")->getChildByName("back"));
    _creditback->deactivate();
    _creditback->setVisible(true);
    _creditback->setListener([=](const std::string& name, bool down) {
        _creditSelected = false;
    });
    
    _instback = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_back"));
    _instback->deactivate();
    _instback->setVisible(true);
    _instback->setListener([=](const std::string& name, bool down) {
        _instSelected = false;
    });
    
    _instboard1 = std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_instboard-1"));
    _instboard1->setVisible(false);
    
    _instboard2= std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_instboard-2"));
    _instboard2->setVisible(false);
    
    _instboard3= std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_instboard-3"));
    _instboard3->setVisible(false);
    
    _instboard4= std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_instboard-4"));
    _instboard4->setVisible(false);
    
    _instboard5= std::dynamic_pointer_cast<Node>(_assets->get<Node>("main_instboard-5"));
    _instboard5->setVisible(false);
    
    _leftarr = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_left"));
    _leftarr->deactivate();
    _leftarr->setVisible(false);
    _leftarr->setListener([=](const std::string& name, bool down) {
        if (inst_num > 1 && ticks - buttoncooldown >= 40) {
            inst_num --;
            buttoncooldown = ticks;
        }
    });
    
    _rightarr = std::dynamic_pointer_cast<Button>(_assets->get<Node>("main_right"));
    _rightarr->deactivate();
    _rightarr->setVisible(false);
    _rightarr->setListener([=](const std::string& name, bool down) {
        //        this->_active = down;        
        if (inst_num < 5 && ticks - buttoncooldown >= 40) {
            CULog("right arrow selected %i", inst_num);
            inst_num ++;
            buttoncooldown = ticks;
        }
    });
    
    addChild(layer);
    
//    Application::get()->setClearColor(Color4(192,192,192,255));
    return true;
}


/**
 * Disposes of all (non-static) resources allocated to this mode.
 */
void MainMenu::dispose() {
    // Deactivate the button (platform dependent)
    if (isPending(_creditbutton)) _creditbutton->deactivate();
    if (isPending(_instbutton)) _instbutton->deactivate();
    if (isPending(_startbutton)) _startbutton->deactivate();
    if (isPending(_levelbutton)) _levelbutton->deactivate();
    if (isPending(_creditback)) _creditback->deactivate();
    if (isPending(_leftarr)) _leftarr->deactivate();
    if (isPending(_rightarr)) _rightarr->deactivate();
    _creditbutton = nullptr;
    _instbutton = nullptr;
    _startbutton = nullptr;
    _levelbutton = nullptr;
    _creditboard = nullptr;
    _creditback = nullptr;
    _instboard1 = nullptr;
    _instboard2 = nullptr;
    _instboard3 = nullptr;
    _instboard4 = nullptr;
    _instboard5 = nullptr;
    _assets = nullptr;
    inst_num = 1;
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
void MainMenu::update(float progress) {
    ticks++;
    _creditbutton->setVisible(true);
    _creditbutton->activate(1000);
    _instbutton->setVisible(true);
    _instbutton->activate(1001);
    _startbutton->setVisible(true);
    _startbutton->activate(1002);
    _levelbutton->setVisible(true);
    _levelbutton->activate(1003);
    
    if (_instSelected) {
        _levelbutton->deactivate();
        _startbutton->deactivate();
        deactivateCredit();
        _leftarr->setVisible(true);
        _leftarr->activate(2000);
        _rightarr->setVisible(true);
        _rightarr->activate(2001);
        _instback->setVisible(true);
        _instback->activate(2002);
        
        if (inst_num == 1) {
            hideinstboards();
            _instboard1->setVisible(true);
        }
        else if (inst_num == 2) {
            hideinstboards();
            _instboard2->setVisible(true);
        }
        else if (inst_num==3) {
            hideinstboards();
            _instboard3->setVisible(true);
        }
        else if (inst_num == 4) {
            hideinstboards();
            _instboard4->setVisible(true);
        }
        else {
            hideinstboards();
            _instboard5->setVisible(true);
        }
    }
    else {
        deactivateInst();
    }
    
    if (!_instSelected && _creditSelected) {
        CULog("credit button");
        deactivateInst();
        _levelbutton->deactivate();
        _startbutton->deactivate();
        
        _creditboard->setVisible(true);
        _creditback->setVisible(true);
        _creditback->activate(1004);
        
    }
    else {
        deactivateCredit();
    }
    
    
}

/**
 * Returns true if loading is complete, but the player has not pressed play
 *
 * @return true if loading is complete, but the player has not pressed play
 */
bool MainMenu::isPending(std::shared_ptr<Button> b) const {
    return b != nullptr && b->isVisible();
    
}

/**
 * Returns the active screen size of this scene.
 *
 * This method is for graceful handling of different aspect
 * ratios
 */
Size MainMenu::computeActiveSize() const {
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

