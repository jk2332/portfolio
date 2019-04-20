//
//  Pest.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/11/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "Pest.hpp"
#include "Constants.hpp"
#include <map>

using namespace std;

map<string, int> pestHealth = {{"snail", 5}, {"raccoon", 15}};
map<string, float> pestSpeed = {{"snail", 0.35f}, {"raccoon", 1.0f}};
map<string, float> pestXOffset = {{"snail", 0.6f}, {"raccoon", 1.4f}};


bool Pest::init(int x, int y, std::string type, string side, float drawscale) {
    _active = false;

    _target = Vec2(x, y);
    _scaledTargetX = (DOWN_LEFT_CORNER_X + GRID_WIDTH*(x) + GRID_WIDTH/2)*32.0f;
    _scale = drawscale;
    _type = type;
    _side = side;
    
    if  (_side == "left") {
        _speed = pestSpeed[type];
        _xside = LEFT;
        _scaledTargetX -= pestXOffset[_type] * 32.0f;
    } else if (_side == "right") {
        _speed = -pestSpeed[type];
        _xside = RIGHT;
        _scaledTargetX += pestXOffset[_type] * 32.0f;
    }

    _health = pestHealth[type];

    _actions = ActionManager::alloc();
    if (type == "snail") {
        _move = Animate::alloc(0, 5, 1.0f, 1);
    } else if (type == "raccoon") {
        _move = Animate::alloc(0, 9, 1.0f, 1);
    }

    return true;
}

void Pest::dispose() {
}

void Pest::walk() {
    if (_active) {
        return;
    } else {
        _active = true;
        _node->setVisible(true);
        _actions->activate("current", _move, _node);
    }
}

void Pest::update(float dt) {
    if (!((_node->getPositionX() < _scaledTargetX && _side == "left") || (_node->getPositionX() > _scaledTargetX && _side == "right"))) {
        _active = false;
    }
    if (_active) {
        CULog("pest updated");
        _actions->update(dt);
        auto pos = _node->getPosition();
        _node->setPosition(Vec2(pos.x + _speed, pos.y));
        _actions->activate("current", _move, _node);
    }
}

void Pest::setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string id){
    if (_type == "snail") {
        if (_side == "left") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 6);
        } else if (_side == "right") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 6);
        }
    } else if (_type == "raccoon") {
        if (_side == "left") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 10);
        } else if (_side == "right") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 10);
        }
    }
    _node->setAnchor(Vec2::ANCHOR_CENTER);
    _node->setScale(1.1f);
    cugl::Vec2 a = Vec2((DOWN_LEFT_CORNER_X + GRID_WIDTH*(_xside) + GRID_WIDTH/2)*32.0f, (0.15f + DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_target.y - GRID_HEIGHT/2)*32.0f);
    _node->setPosition(a);

    node->addChildWithName(_node, id);
}
