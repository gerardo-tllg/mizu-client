#version 330 core

uniform float t;
uniform vec2 res;
out vec4 fragColor;

float h(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float n(vec2 p){
  vec2 i=floor(p);vec2 f=fract(p);
  vec2 u=f*f*f*(f*(f*6.0-15.0)+10.0);
  return mix(mix(h(i),h(i+vec2(1,0)),u.x),mix(h(i+vec2(0,1)),h(i+vec2(1,1)),u.x),u.y)*2.0-1.0;
}
float fbm(vec2 p){
  float v=0.0;float a=0.5;
  for(int i=0;i<5;i++){v+=a*n(p);p=p*2.1+vec2(1.7,9.2);a*=0.5;}
  return v;
}
float peak(float x,float cx,float width,float height){
  float d=(x-cx)/width;
  return height*exp(-d*d*3.5);
}
float waveSurf(float px,float tm,float scale){
  float slow=tm*0.18;
  float p1=peak(px,0.08+sin(slow*0.7)*0.04,0.14,0.22*scale);
  float p2=peak(px,0.28+sin(slow*0.5+1.2)*0.05,0.18,0.28*scale);
  float p3=peak(px,0.52+sin(slow*0.6+0.8)*0.04,0.22,0.20*scale);
  float p4=peak(px,0.72+sin(slow*0.4+2.1)*0.05,0.16,0.25*scale);
  float p5=peak(px,0.92+sin(slow*0.55+1.5)*0.03,0.12,0.18*scale);
  float ripple=sin(px*22.0-tm*1.8)*0.010*scale+sin(px*35.0+tm*2.2)*0.006*scale;
  float turb=fbm(vec2(px*2.2+slow*0.3,slow*0.15))*0.020*scale;
  return p1+p2+p3+p4+p5+ripple+turb;
}
void main(){
  vec2 uv=gl_FragCoord.xy/res;
  float px=uv.x;
  float tm=t;
  float surf1=0.28+waveSurf(px,tm,1.0);
  float surf2=0.22+waveSurf(px,tm*1.1+0.5,0.78);
  float surf3=0.17+waveSurf(px,tm*0.9+1.2,0.58);
  float surf4=0.13+waveSurf(px,tm*1.2+2.1,0.40);
  float surf5=0.09+waveSurf(px,tm*0.8+3.0,0.25);
  float b1=step(uv.y,surf1);
  float b2=step(uv.y,surf2);
  float b3=step(uv.y,surf3);
  float b4=step(uv.y,surf4);
  float b5=step(uv.y,surf5);
  float above=1.0-b1;
  vec3 bg=vec3(0.004,0.016,0.050);
  vec3 layer1=vec3(0.006,0.028,0.075);
  vec3 layer2=vec3(0.009,0.042,0.105);
  vec3 layer3=vec3(0.013,0.060,0.138);
  vec3 layer4=vec3(0.018,0.082,0.168);
  vec3 layer5=vec3(0.024,0.108,0.200);
  vec3 lineCol1=vec3(0.05,0.65,0.50);
  vec3 lineCol2=vec3(0.04,0.55,0.42);
  vec3 lineCol3=vec3(0.04,0.45,0.35);
  vec3 lineCol4=vec3(0.035,0.35,0.28);
  vec3 lineCol5=vec3(0.028,0.28,0.22);
  vec3 col=bg;
  col=mix(col,layer1,b1);
  col=mix(col,layer2,b2);
  col=mix(col,layer3,b3);
  col=mix(col,layer4,b4);
  col=mix(col,layer5,b5);
  float lw=3.0/res.y;
  float fn1=n(vec2(px*10.0+tm*0.4,tm*0.28))*0.5+0.5;
  float fn2=n(vec2(px*18.0-tm*0.35,tm*0.22))*0.5+0.5;
  float line1=smoothstep(lw*6.0,0.0,abs(uv.y-surf1));
  float line2=smoothstep(lw*5.0,0.0,abs(uv.y-surf2));
  float line3=smoothstep(lw*4.0,0.0,abs(uv.y-surf3));
  float line4=smoothstep(lw*3.0,0.0,abs(uv.y-surf4));
  float line5=smoothstep(lw*2.5,0.0,abs(uv.y-surf5));
  col=mix(col,lineCol1*(0.8+fn1*0.4),line1*0.9);
  col=mix(col,lineCol1+vec3(0.04,0.18,0.14)*fn1*fn2,line1*fn1*0.5);
  col=mix(col,lineCol2*(0.75+fn2*0.35),line2*0.8);
  col=mix(col,lineCol3*(0.7+fn1*0.3),line3*0.72);
  col=mix(col,lineCol4*(0.65+fn2*0.25),line4*0.62);
  col=mix(col,lineCol5*0.6,line5*0.5);
  float sprayZ=smoothstep(surf1+0.001,surf1-0.018,uv.y)*above;
  float spN=step(0.72,n(vec2(px*38.0+tm*0.7,uv.y*25.0))*0.5+0.5);
  col+=vec3(0.05,0.35,0.28)*spN*sprayZ*line1*0.35;
  float stars=step(0.9982,h(vec2(floor(px*220.0),floor(uv.y*220.0))));
  col+=vec3(0.18,0.50,0.42)*stars*above*smoothstep(surf1+0.02,surf1+0.06,uv.y);
  float rain=step(0.9982,h(vec2(floor(px*250.0+tm*10.0),floor((uv.y+tm*1.0)*140.0))));
  col+=vec3(0.10,0.30,0.26)*rain*above*0.15;
  float moon=1.0-smoothstep(0.0,0.025,length(vec2(px-0.85,uv.y-0.82)));
  float moonGlow=1.0-smoothstep(0.0,0.09,length(vec2(px-0.85,uv.y-0.82)));
  col+=vec3(0.14,0.45,0.36)*moon*above*0.5;
  col+=vec3(0.03,0.09,0.07)*moonGlow*above*0.22;
  col=pow(max(col,0.0),vec3(0.88));
  fragColor=vec4(col,1.0);
}
