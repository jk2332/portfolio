//  LevelSelect.hpp
//  WeatherDefender
//
//  Created by 김지원 on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef MainMenu_hpp
#define MainMenu_hpp

#include <stdio.h>
#include <cugl/cugl.h>

//Music
#define MAIN_MENU_MUSIC     "mainMenu"
#define MUSIC_VOLUME   0.6


#pragma mark -
#pragma mark GameController
/**
 * This class is a simple loading screen for asychronous asset loading.
 *
 * The screen will display a very minimal progress bar that displays the
 * status of the asset manager.  Make sure that all asychronous load requests
 * are issued BEFORE calling update for the first time, or else this screen
 * will think that asset loading is complete.
 *
 * Once asset loading is completed, it will display a play button.  Clicking
 * this button will inform the application root to switch to the gameplay mode.
 */
class MainMenu : public cugl::Scene {
protected:
    /** The asset manager for loading. */
    std::shared_ptr<cugl::AssetManager> _assets;
    cugl::Size _dimen;
    
    // NO CONTROLLER (ALL IN SEPARATE THREAD)
    
    std::shared_ptr<cugl::Button> _startbutton;
    std::shared_ptr<cugl::Button> _instbutton;
    std::shared_ptr<cugl::Button> _creditbutton;
    std::shared_ptr<cugl::Button> _levelbutton;
    
    long buttoncooldown = 0l;
    long ticks = 01;
    std::shared_ptr<cugl::Node> _creditboard;
    
    std::shared_ptr<cugl::Node> _instboard1;
    std::shared_ptr<cugl::Node> _instboard2;
     std::shared_ptr<cugl::Node> _instboard3;
     std::shared_ptr<cugl::Node> _instboard4;
     std::shared_ptr<cugl::Node> _instboard5;
    
    std::shared_ptr<cugl::Button> _leftarr;
    std::shared_ptr<cugl::Button> _rightarr;
    
    std::shared_ptr<cugl::Button> _creditback;
    std::shared_ptr<cugl::Button> _instback;
    

    float _scale;
    bool _startSelected = false;
    bool _instSelected= false;
    bool _creditSelected = false;
    bool _levelSelected = false;
    int inst_num = 1;
    
    /**
     * Returns the active screen size of this scene.
     *
     * This method is for graceful handling of different aspect
     * ratios
     */
    cugl::Size computeActiveSize() const;
    
    
public:
#pragma mark -
#pragma mark Constructors
    /**
     * Creates a new loading mode with the default values.
     *
     * This constructor does not allocate any objects or start the game.
     * This allows us to use the object without a heap pointer.
     */
    MainMenu() : Scene() {}
    
    /**
     * Disposes of all (non-static) resources allocated to this mode.
     *
     * This method is different from dispose() in that it ALSO shuts off any
     * static resources, like the input controller.
     */
    ~MainMenu() { dispose(); }
    
    /**
     * Disposes of all (non-static) resources allocated to this mode.
     */
    void dispose();
    
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
    bool init(const std::shared_ptr<cugl::AssetManager>& assets);
    
    
#pragma mark -
#pragma mark Progress Monitoring
    /**
     * The method called to update the game mode.
     *
     * This method updates the progress bar amount.
     *
     * @param timestep  The amount of time (in seconds) since the last frame
     */
    void update(float timestep);
    
    /**
     * Returns true if loading is complete, but the player has not pressed play
     *
     * @return true if loading is complete, but the player has not pressed play
     */
    bool isPending(std::shared_ptr<cugl::Button> b) const;
    bool creditSelected(){return _creditSelected;}
    bool startSelected(){return _startSelected;}
    bool instSelected(){return _instSelected;}
    bool levelSelected(){return _levelSelected;}
    void resetSelectBool() {
        _startSelected=false;
        _levelSelected = false;
    }
    
    void hideinstboards(){
        _instboard1->setVisible(false);
        _instboard2->setVisible(false);
        _instboard3->setVisible(false);
        _instboard4->setVisible(false);
        _instboard5->setVisible(false);
    }
    
    void deactivateInst(){
        _leftarr->deactivate();
        _rightarr->deactivate();
        _leftarr->setVisible(false);
        _rightarr->setVisible(false);
        _instback->setVisible(false);
        _instback->deactivate();
        hideinstboards();
    }
    
    void deactivateCredit(){
        _creditboard->setVisible(false);
        _creditback->setVisible(false);
        _creditback->deactivate();
    }
};

#endif /* LevelSelect_hpp */

