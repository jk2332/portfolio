//
//  InputController.h
//  Weather Defender
//
//  This input controller is primarily designed for keyboard control.  On mobile
//  you will notice that we use gestures to emulate keyboard commands. They even
//  use the same variables (though we need other variables for internal keyboard
//  emulation).  This simplifies our design quite a bit.
//
//  This file is based on the CS 3152 PhysicsDemo Lab by Don Holden, 2007
//
//  Author: Walker White and Anthony Perello
//  Version: 1/26/17
//
#include "InputController.h"

using namespace cugl;

#pragma mark Input Constants

/** The key to use for reseting the game */
#define RESET_KEY KeyCode::R
/** The key for toggling the debug display */
#define DEBUG_KEY KeyCode::D
/** The key for exitting the game */
#define EXIT_KEY  KeyCode::ESCAPE
/** The key for splitting the cloud */
#define SPLIT_KEY   KeyCode::S
/** The key for splitting the cloud */
#define JOIN_KEY   KeyCode::J

/** How fast a double click must be in milliseconds */
#define EVENT_DOUBLE_CLICK  400
/** How far we must swipe left or right for a gesture */
#define EVENT_SWIPE_LENGTH  100
/** How fast we must swipe left or right for a gesture */
#define EVENT_SWIPE_TIME   1000
/** How far we must turn the tablet for the accelerometer to register */
#define EVENT_ACCEL_THRESH  M_PI/10.0f
/** The key for the event handlers */
#define LISTENER_KEY        1

#pragma mark -
#pragma mark Input Controller
/**
 * Creates a new input controller.
 *
 * This constructor does NOT do any initialzation.  It simply allocates the
 * object. This makes it safe to use this class without a pointer.
 */
RagdollInput::RagdollInput() :
_active(false),
_resetPressed(false),
_pinched(false),
_debugPressed(false),
_exitPressed(false),
_keyUp(false),
_keyDown(false),
_keyReset(false),
_keySplit(false),
_keyJoin(false),
_splitPressed(false),
_keyDebug(false),
_keyExit(false),
//_isBeingPinched(false),
//_endedPinch(false),
_touchID(-1) {
}

/**
 * Deactivates this input controller, releasing all listeners.
 *
 * This method will not dispose of the input controller. It can be reused
 * once it is reinitialized.
 */
void RagdollInput::dispose() {
    if (_active) {
#ifndef CU_TOUCH_SCREEN
        Input::deactivate<Keyboard>();
        Mouse* mouse = Input::get<Mouse>();
        mouse->removePressListener(LISTENER_KEY);
        mouse->removeReleaseListener(LISTENER_KEY);
        mouse->removeDragListener(LISTENER_KEY);
#else
        Input::deactivate<Accelerometer>();
        Touchscreen* touch = Input::get<Touchscreen>();
        touch->removeBeginListener(LISTENER_KEY);
        touch->removeEndListener(LISTENER_KEY);
        PinchInput* pi = Input::get<PinchInput>();
        pi->removeEndListener(LISTENER_KEY);
        pi->removeBeginListener(LISTENER_KEY);
        pi->removeChangeListener(LISTENER_KEY);
#endif
        _active = false;
    }
}

/**
 * Initializes the input control for the given drawing scale.
 *
 * This method works like a proper constructor, initializing the input
 * controller and allocating memory.  However, it still does not activate
 * the listeners.  You must call start() do that.
 *
 * @return true if the controller was initialized successfully
 */
bool RagdollInput::init() {
    for(std::vector<int>::size_type i = 0; i != _timestamps.size(); i++) {
        _timestamps.at(i).mark();
    }
    _pinchTimestamp.mark();
    bool success = true;
    
#ifndef CU_TOUCH_SCREEN
    success = Input::activate<Keyboard>();
    success = success && Input::activate<Mouse>();
    Mouse* mouse = Input::get<Mouse>();
    // Set pointer awareness to always so listening for drags registers
    // See addDragListener for an explanation
    mouse->setPointerAwareness(cugl::Mouse::PointerAwareness::ALWAYS);
    mouse->addPressListener(LISTENER_KEY, [=](const cugl::MouseEvent& event, Uint8 clicks, bool focus) {
        this->mousePressBeganCB(event, clicks, focus);
    });
    mouse->addReleaseListener(LISTENER_KEY, [=](const cugl::MouseEvent& event, Uint8 clicks, bool focus) {
        this->mouseReleasedCB(event, clicks, focus);
    });
    mouse->addDragListener(LISTENER_KEY, [=](const cugl::MouseEvent& event, const cugl::Vec2& previous, bool focus) {
        this->mouseDraggedCB(event, previous, focus);
    });
#else
    success = Input::activate<Accelerometer>();
    Touchscreen* touch = Input::get<Touchscreen>();
    touch->addBeginListener(LISTENER_KEY,[=](const cugl::TouchEvent& event, bool focus) {
        this->touchBeganCB(event,focus);
    });
    touch->addEndListener(LISTENER_KEY,[=](const cugl::TouchEvent& event, bool focus) {
        this->touchEndedCB(event,focus);
    });
    touch->addMotionListener(LISTENER_KEY,[=](const cugl::TouchEvent& event, const cugl::Vec2& previous, bool focus) {
        this->touchesMovedCB(event, previous, focus);
    });
    success = success && Input::activate<PinchInput>();
    PinchInput* pinput = Input::get<PinchInput>();
    pinput->addBeginListener(LISTENER_KEY, [=](const cugl::PinchEvent& event, bool focus) {
        this->pinchBeganCB(event, focus);
    });
    pinput->addEndListener(LISTENER_KEY, [=](const PinchEvent& event, bool focus) {
        this->pinchEndCB(event, focus);
    });
    pinput->addChangeListener(LISTENER_KEY, [=](const PinchEvent& event, bool focus){
        this->pinchChangeCB(event, focus);
    });
#endif
    _active = success;
    return success;
}


/**
 * Processes the currently cached inputs.
 *
 * This method is used to to poll the current input state.  This will poll the
 * keyboad and accelerometer.
 *
 * This method also gathers the delta difference in the touches. Depending on
 * the OS, we may see multiple updates of the same touch in a single animation
 * frame, so we need to accumulate all of the data together.
 */
void RagdollInput::update(float dt) {
#ifndef CU_TOUCH_SCREEN
    // DESKTOP CONTROLS
    Keyboard* keys = Input::get<Keyboard>();
    
    // Map "keyboard" events to the current frame boundary
    _keyReset  = keys->keyPressed(RESET_KEY);
    _keySplit = keys->keyPressed(SPLIT_KEY);
    _keyJoin = keys->keyPressed(JOIN_KEY);
    _keyDebug  = keys->keyPressed(DEBUG_KEY);
    _keyExit   = keys->keyPressed(EXIT_KEY);
    
    //    PinchInput* pi = Input::get<PinchInput>();
    //    _isBeingPinched = pi->isActive();
    
#endif
    
    _resetPressed = _keyReset;
    _debugPressed = _keyDebug;
    _exitPressed  = _keyExit;
    _splitPressed = _keySplit;
    _joinPressed = _keyJoin;
    //    _pinched = _isBeingPinched;
    
    
    // If it does not support keyboard, we must reset "virtual" keyboard
#ifdef CU_TOUCH_SCREEN
    _keyExit = false;
    _keyReset = false;
    _keyDebug = false;
    _keySplit = false;
    _keyJoin = false;
    
#endif
}

/**
 * Clears any buffered inputs so that we may start fresh.
 */
void RagdollInput::clear() {
    _resetPressed = false;
    _debugPressed = false;
    _exitPressed  = false;
    _pinched = false;
    //    for(std::vector<int>::size_type i = 0; i != _timestamps.size(); i++) {
    //        _timestamps.at(i).mark();
    //        _selects.at(i) = false;
    //    }
    for (auto & timestamp : _timestamps) {
        timestamp.second.mark();
    }
    for (auto & dtouch : _dtouches) {
        dtouch.second = Vec2::ZERO;
    }
    _selects.clear();
    _pinchTimestamp.mark();
}

#pragma mark -
#pragma mark Touch and Mouse Callbacks
/**
 * Callback for the beginning of a touch event
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::touchBeganCB(const cugl::TouchEvent& event, bool focus) {
    // Time how long it has been since last start touch, and check for enabling debug mode on mobile.
    //    _keyDebug = event.timestamp.ellapsedMillis(_timestamp) <= EVENT_DOUBLE_CLICK;
    //    _timestamps.push_back(event.timestamp);
    // if there is currently no touch for a selection
    //    if (_touchID == -1) {
    //        _touchID = event.touch;
    //        touchBegan(event.timestamp, event.position);
    //    }
    //    _touchIDs.push_back(event.touch);
    
    
    //    _keyDebug = event.timestamp.ellapsedMillis(_timestamps[event.touch]) <= EVENT_DOUBLE_CLICK;
    //    _timestamps[event.touch] = event.timestamp;
    //    if (!_touchIDs[event.touch]){
    //        _touchIDs[event.touch] = true;
    //        touchBegan(event.touch, event.timestamp, event.position);
    //    }
    
    //    _keyDebug = event.timestamp.ellapsedMillis(_timestamps.at(event.touch)) <= EVENT_DOUBLE_CLICK;
    //    CULog("%f touch x", event.position.x);
    //    CULog("%f touch y", event.position.y);
    if (!_timestamps.count(event.touch)){
        _timestamps.insert({event.touch, event.timestamp});
    } else {
        _timestamps.at(event.touch) = event.timestamp;
    }
    if (!_touchIDs.count(event.touch)){
        _touchIDs.insert(event.touch);
    }
    touchBegan(event.touch, event.timestamp, event.position);
}

/**
 * Callback for a mouse press event.
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::mousePressBeganCB(const cugl::MouseEvent& event, Uint8 clicks, bool focus) {
    if (!_touchIDs.count(0)){
        _touchIDs.insert(0);
    }
    touchBegan(0, event.timestamp, event.position);
}
void RagdollInput::pinchBeganCB(const cugl::PinchEvent& event, bool focus){
    _pinchTimestamp.mark();
    _pinched = true;
    if (!_pinchSelect){
        _dpinch = event.position;
    }
    _pinchSelect = true;
}

void RagdollInput::pinchEndCB(const cugl::PinchEvent& event, bool focus){
    //    CULog("pinch ended");
    //    _dpinch = event.position;
    //    CULog("dpinch x: %f", _dpinch.x/41.6875);
    //    CULog("dpinch y: %f", 18 - _dpinch.y/41.666667);
    _pinchSelect = false;
    _pinched = false;
    
}

void RagdollInput::pinchChangeCB(const cugl::PinchEvent& event, bool focus){
    //    CULog("pinch changed");
    if (event.timestamp.ellapsedMillis(_pinchTimestamp) > 600){
        _pinched = false;
        _pinchSelect = false;
    }
}



/**
 * Handles touchBegan and mousePress events using shared logic.
 *
 * Depending on the platform, the appropriate callback (i.e. touch or mouse) will call into this method to handle the Event.
 *
 * @param timestamp     the timestamp of the event
 * @param pos         the position of the touch
 */
void RagdollInput::touchBegan(long touchID, const cugl::Timestamp timestamp, const cugl::Vec2& pos) {
    // All touches correspond to key up
    _keyUp = true;
    //    _timestamps.at(touchID) = timestamp;
    //    CULog("touch began %i", touchID);
    if (!_timestamps.count(touchID)){
        _timestamps.insert({touchID, timestamp});
    } else {
        _timestamps.at(touchID) = timestamp;
    }
    // Update the touch location for later gestures
    if (!_selects.count(touchID)) {
        if (!_dtouches.count(touchID)){
            _dtouches.insert({touchID, pos});
        } else {
            _dtouches.at(touchID) = pos;
        }
    }
    if (!_selects.count(touchID)){
        _selects.insert(touchID);
    }
}


/**
 * Callback for the end of a touch event
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::touchEndedCB(const cugl::TouchEvent& event, bool focus) {
    //    if (event.touch == _touchID) {
    //        touchEnded(event.timestamp, event.position);
    //        _touchID = -1;
    //    }
    //    if (_touchIDs.count(event.touch)){
    //        _touchIDs.erase(event.touch);
    //    }
    touchEnded(event.touch, event.timestamp, event.position);
}

/**
 * Callback for a mouse release event.
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::mouseReleasedCB(const cugl::MouseEvent& event, Uint8 clicks, bool focus) {
    //    if (_touchIDs.count(0)){
    //        _touchIDs.erase(0);
    //    }
    touchEnded(0, event.timestamp, event.position);
}

/**
 * Handles touchEnded and mouseReleased events using shared logic.
 *
 * Depending on the platform, the appropriate callback (i.e. touch or mouse) will call into this method to handle the Event.
 *
 * @param timestamp     the timestamp of the event
 * @param pos         the position of the touch
 */
void RagdollInput::touchEnded(long touchID, const cugl::Timestamp timestamp, const cugl::Vec2& pos) {
    //    CULog("touch ended %i", touchID);
    _selects.erase(touchID);
    if (_touchIDs.empty()){
        _keyUp = false;
    }
}

/**
 * Callback for a touch moved event.
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::touchesMovedCB(const cugl::TouchEvent& event, const Vec2& previous, bool focus) {
    //    if (event.touch == _touchID) {
    //        touchMoved(event.position);
    //    }
    if (_touchIDs.count(event.touch)){
        touchMoved(event.touch, event.position);
    }
}

/**
 * Callback for a mouse drag event.
 *
 * @param t     The touch information
 * @param event The associated event
 */
void RagdollInput::mouseDraggedCB(const cugl::MouseEvent& event, const Vec2& previous, bool focus) {
    if (_touchIDs.count(0)){
        touchMoved(0, event.position);
    }
}

/**
 * Handles touchMoved and mouseDragged events using shared logic.
 *
 * Depending on the platform, the appropriate callback (i.e. touch or mouse) will call into this method to handle the Event.
 *
 * @param timestamp     the timestamp of the event
 * @param pos         the position of the touch
 */
void RagdollInput::touchMoved(long touchID, const cugl::Vec2& pos) {
    //    CULog("touch moved %i", touchID);
    if (!_dtouches.count(touchID)){
        _dtouches.insert({touchID, pos});
    } else {
        _dtouches.at(touchID) = pos;
    }
}








