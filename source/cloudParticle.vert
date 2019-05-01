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

uniform vec2 offset;
uniform vec4 color;
uniform vec3 aspectRatio;
                                     
void main(){
    float scaleX = 1.0f;
    float scaleY = aspectRatio.x/aspectRatio.y;
    float offsetScaleX = scaleX/2.575f;
    float offsetScaleY = scaleY/2.575f;
    if (aspectRatio.z == 1.0f){
        scaleX = aspectRatio.y/aspectRatio.x;
        scaleY = 1.0f;
        offsetScaleX = scaleX/2.9f;
        offsetScaleY = scaleY/2.9f;
    }
    TexCoords = texCoords;
    ParticleColor = color;
    gl_Position = vec4(scaleX*position.x+offsetScaleX*offset.x, scaleY*position.y+offsetScaleY*offset.y, 0,1);
}

/////////// SHADER END //////////
);
