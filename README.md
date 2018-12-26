# modulationTest

Just messing around, trying to get some simple Amplitude Modulation to work.
I really hate this code because of the hard-coded values. I just don't have a firm enough grasp on the math to create more elegant code at the moment.

Next up, after I get a good grasp of AM, I will work on FM.

Results (Pure Wave vs Amplitude Modulated Wave):
Sine Wave:
![sine_wave](https://user-images.githubusercontent.com/10580033/50458130-b0ff5f00-0915-11e9-80c7-a42eba265a9b.jpg)

Square Wave:
![square_wave](https://user-images.githubusercontent.com/10580033/50458131-b0ff5f00-0915-11e9-9cde-93c1cce171db.jpg)

Sawtooth Wave:
![sawtooth_wave](https://user-images.githubusercontent.com/10580033/50458129-b0ff5f00-0915-11e9-8c93-e7e0e426ecb0.jpg)

Triangle Wave:
![triangle_wave](https://user-images.githubusercontent.com/10580033/50458132-b197f580-0915-11e9-871a-e8570eb4435e.jpg)

Semi-circle (My bessel-J function vs a real bessel-J):
![fourier_semicircle](https://user-images.githubusercontent.com/10580033/50458128-b0ff5f00-0915-11e9-9e3f-e241c862e030.jpg)
My naive bessel-J implementation (just straight computing the summation) is only good between x=0 and around x=8 before it diverges significantly. To get a basic approximation of a proper semicircle we need to be able to handle at least x=PI*20
