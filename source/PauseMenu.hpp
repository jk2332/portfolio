//  LevelSelect.hpp
//  WeatherDefender
//
//  Created by 김지원 on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef PauseMenu_hpp
#define PauseMenu_hpp

#include <stdio.h>

#include <cugl/cugl.h>

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
class PauseMenu : public cugl::Scene {
protected:
    /** The asset manager for loading. */
    std::shared_ptr<cugl::AssetManager> _assets;
    
    // NO CONTROLLER (ALL IN SEPARATE THREAD)
   
//    std::shared_ptr<cugl::Button> _backToMainButton;
    std::shared_ptr<cugl::Button> _quitButton;
    std::shared_ptr<cugl::Button> _continueButton;
    std::shared_ptr<cugl::Button> _backToLevelButton;
//    std::shared_ptr<cugl::Node> _quitButtonNode;
//    std::shared_ptr<cugl::Node> _continueButtonNode;
//    std::shared_ptr<cugl::Node> _backToLevelButtonNode;

    bool _assetLoaded = false;
    bool _continueSelected;
    bool _backToLevelSelected;
    float _scale;
    
    
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
    PauseMenu() : Scene() {}
    
    /**
     * Disposes of all (non-static) resources allocated to this mode.
     *
     * This method is different from dispose() in that it ALSO shuts off any
     * static resources, like the input controller.
     */
    ~PauseMenu() { dispose(); }
    
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
    bool backToLevelSelected(){return _backToLevelSelected;}
    bool continueSelected(){return _continueSelected;}
    void setAssetLoaded(bool b) {_assetLoaded = b;}
};

#endif /* LevelSelect_hpp */

