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
    _state = noNeed;
    isShaded = false;
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

void Plant::updateState(int state){
    _state = state;
}




