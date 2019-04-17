//
//  Constants.hpp
//  WeatherDefender
//
//  Created by Zaibo  Wang on 4/17/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#ifndef Constants_hpp
#define Constants_hpp

// Pests
#define LEFT -2
#define RIGHT 10

// Plants
#define noNeed 0
#define needRain 1
#define needSun 2
#define needShade 3
#define dead 4

// Level Model
#define DYNAMIC_COLOR   Color4::GREEN
#define X_COORD   "x"
#define Y_COORD   "y"
#define ID   "id"
#define CLOUDS_FIELD   "cloud"
#define CLOUD_PRIORITY   4
#define TEXTURE_FIELD   "texture"
#define HEIGHT_FIELD   "height"
#define WIDTH_FIELD   "width"
#define TYPE   "type"
#define GRID_NUM_X          9
#define GRID_NUM_Y          3

// Board
/** Width of the game world in Box2d units */
#define DEFAULT_WIDTH   32.0f
/** Height of the game world in Box2d units */
#define DEFAULT_HEIGHT  18.0f
#define GRID_WIDTH      3
#define GRID_HEIGHT     2.5f
#define DRAW_WIDTH     3
#define DRAW_HEIGHT     2
#define DOWN_LEFT_CORNER_X    3
#define DOWN_LEFT_CORNER_Y    4
#define OFFSET_X         0
#define OFFSET_Y         0

#define SIGN  "sign"

#endif /* Constants_hpp */
