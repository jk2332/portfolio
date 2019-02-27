//
//  Plant.cpp
//  RagdollDemo (Mac)
//
//  Created by 김지원 on 2/23/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Plant.hpp"

#include <Box2D/Dynamics/Joints/b2RevoluteJoint.h>
#include <Box2D/Dynamics/Joints/b2WeldJoint.h>
#include <Box2D/Dynamics/b2World.h>


using namespace cugl;

bool Plant::init(const Vec2& pos) {
    Obstacle::init(pos);
    _health = 0;
    //_texture = texture;
    isShaded = false;
    _state = rand() % 4;
    if (_state == needRain){
        _state = needShade;
    }
    return true;
}

/**
 * Disposes all resources and assets of this Ragdoll
 *
 * Any assets owned by this object will be immediately released.  Once
 * disposed, a Ragdoll may not be used until it is initialized again.
 */
void Plant::dispose() {
    //_texture = nullptr;
}

void Plant::updateState(){
    if (_state == needShade){
        if (isShaded){
            incHealth();
            if (_health >= 0){_state = noNeed;}
        }
        else{decHealth();}
    }
    else if (_state == needRain){
        //nothing yet
    }
    else if (_state == needSun){
        if (!isShaded){
            incHealth();
            if (_health >= 0){_state = noNeed;}
        }
        else{decHealth();}
    }
//    CULog("%d", _health);
}

void Plant::setState(int s){
    _state = s;
    _health = 0;
}



