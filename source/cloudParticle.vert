//
//  cloudParticle.vert
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/13/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

/**
 * Uniform texture shader for OpenGL and OpenGL ES
 */
const char* const oglCTVert = SHADER(
////////// SHADER BEGIN /////////

//layout (location = 0)
in vec2 position;
in vec2 texCoords;
                                         
out vec2 TexCoords;
out vec4 ParticleColor;

uniform mat4 projection;
uniform vec2 offset;
uniform vec4 color;

void main(){
    float scaleX = 0.005f;
    float scaleY = scaleX*1.75f;
    float offsetScaleX = scaleX/2.55f;
    float offsetScaleY = scaleY/2.55f;
//    float offsetScaleX = 1.0f;
//    float offsetScaleY = 1.0f;
    TexCoords = texCoords;
    ParticleColor = color;
// gl_Position = projection * vec4((position * scale) + offset, 0.0, 1.0);
    gl_Position = vec4(scaleX*position.x+offsetScaleX*offset.x, scaleY*position.y+offsetScaleY*offset.y, 0,1);
}

/////////// SHADER END //////////
);
