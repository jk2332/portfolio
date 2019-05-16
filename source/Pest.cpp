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
map<string, float> pestSpeed = {{"snail", 0.45f}, {"raccoon", 1.0f}};
map<string, float> pestXOffset = {{"snail", 1.0f}, {"raccoon", 2.2f}};


bool Pest::init(int x, int y, std::string type, string side, float drawscale) {
    _active = false;
    _target = Vec2(x, y);
    //scale in setSceneNode instead
    _scaledTargetX = (DOWN_LEFT_CORNER_X + GRID_WIDTH * x + GRID_WIDTH/2 + GRID_OFFSET_X * x);
    if(type == "rabbit"){_type = "raccoon";}
    else {_type = type;}
    _side = side;
    _state = INACTIVE;
    _attacked = false;
    _ate = false;
    
    if(_side == "left") {
        _speed = pestSpeed[type];
        _xside = LEFT;
        //scale in setSceneNode instead
        _scaledTargetX -= pestXOffset[_type];
    } else if (_side == "right") {
        _speed = -pestSpeed[type];
        _xside = RIGHT;
        //scale in setSceneNode instead
        _scaledTargetX += pestXOffset[_type];
    }

    _health = pestHealth[type];

    _actions = ActionManager::alloc();
    if (type == "snail") {
        _move = Animate::alloc(0, 5, 1.0f, 1);
        _eat = Animate::alloc(0, 14, 2.0f, 1);
    } else if (type == "raccoon") {
        _move = Animate::alloc(0, 9, 1.0f, 1);
        _eat = Animate::alloc(0, 14, 2.0f, 1);
    }

    return true;
}

void Pest::dispose() {
    _active = false;
    _health = pestHealth[_type];
    
    texture = nullptr;
    _actions = nullptr;
    _move = nullptr;
    _assets = nullptr;
    _node = nullptr;
    _node_rev = nullptr;
    _eat_node = nullptr;
    
}

void Pest::walk() {
    if (_attacked) {
        return;
    } else {
        _attacked = true;
        _state = ATTACKING;
        _node->setVisible(true);
        _actions->activate("current", _move, _node);
    }
}

void Pest::update(float dt) {
    if (!_ate && !((_node->getPositionX() < _scaledTargetX && _side == "left") || (_node->getPositionX() > _scaledTargetX && _side == "right"))) {
        // Stop when target reached
        _state = EATING;
        _ate = true;
            // _node = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 6);
    }

    if (_state == ATTACKING) {
        _actions->update(dt);
        auto pos = _node->getPosition();
        _node->setPosition(Vec2(pos.x + _speed, pos.y));
        _node_rev->setPosition(Vec2(pos.x + _speed, pos.y));
        _eat_node->setPosition(Vec2(pos.x + _speed, pos.y));
        _actions->activate("current", _move, _node);
    } else if (_state == RUNNING) {
        _node->setVisible(false);
        _eat_node->setVisible(false);
        _node_rev->setVisible(true);
        _actions->update(dt);
        auto pos = _node_rev->getPosition();
        _node_rev->setPosition(Vec2(pos.x - 2*_speed, pos.y));
        _actions->activate("current", _move, _node_rev);
    } else if (_state == EATING) {
        _node->setVisible(false);
        _node_rev->setVisible(false);
        _eat_node->setVisible(true);
        _actions->update(dt);
        _actions->activate("current", _eat, _eat_node);
    }
}


void Pest::setScared(bool b) {
    if (_state == EATING) {
        _state = RUNNING;
    }
}

bool Pest::checkTarget(shared_ptr<Node> worldNode, shared_ptr<Node> gridNode) {
    if (_state == ATTACKING || _state == RUNNING) {
        return false;
    }
    Vec2 sc = worldNode->nodeToWorldCoords(_node->getPosition());
    Vec2 gridPos = worldNode->nodeToWorldCoords(gridNode->getPosition());
    float a = _node->getWidth()/2;
    float b = _node->getHeight()/2;
    
    int p = (pow((gridPos.x - sc.x), 2) / pow(a, 2)) + (pow((gridPos.y - sc.y), 2) / pow(b, 2));
    //inside
    if (p < 1){return true;}
    //outside
    else{return false;}
}

void Pest::setSceneNode(const std::shared_ptr<cugl::Node>& node, std::string id, float ds){
    _drawscale = ds;
    _scaledTargetX *= ds;
    _eat_node = AnimationNode::alloc(_assets->get<Texture>(_type + "eat"), 1, 15);
    if (_type == "snail") {
        if (_side == "left") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 6);
            _node_rev = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 6);
        } else if (_side == "right") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 6);
            _node_rev = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 6);
        }
    } else if (_type == "raccoon") {
        if (_side == "left") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 10);
            _node_rev = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 10);
        } else if (_side == "right") {
            _node = AnimationNode::alloc(_assets->get<Texture>(_type + "rev"), 1, 10);
            _node_rev = AnimationNode::alloc(_assets->get<Texture>(_type), 1, 10);
        }
    }
    _node->setAnchor(Vec2::ANCHOR_CENTER);
    _node->setScale(1.1f);
    cugl::Vec2 a = _drawscale*Vec2(DOWN_LEFT_CORNER_X + GRID_WIDTH*(_xside) + GRID_WIDTH/2 + GRID_OFFSET_X*_xside, 0.15f + DOWN_LEFT_CORNER_Y + GRID_HEIGHT*_target.y);
    _node->setPosition(a);

    _node_rev->setAnchor(Vec2::ANCHOR_CENTER);
    _node_rev->setScale(1.1f);
    _node_rev->setPosition(a);
    _node_rev->setVisible(false);

    _eat_node->setAnchor(Vec2::ANCHOR_CENTER);
    _eat_node->setScale(0.45);
    _eat_node->setPosition(a);
    _eat_node->setVisible(false);

    int pestZ = Z_PEST_BACK;
    if(_target.y == 1){
        pestZ = Z_PEST_MIDDLE;
    }
    else if(_target.y == 0){
        pestZ = Z_PEST_FRONT;
    }
    node->addChildWithName(_node, id, pestZ);
    node->addChildWithName(_node_rev, id + "rev", pestZ);
    node->addChildWithName(_eat_node, id + "eat", pestZ);
}
