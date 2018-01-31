function onChangeInput(view) {
	var input = document.getElementById("imgInputCode");
	var preview = document.getElementById("previewCode");
	
	if (view == 1)
		document.getElementById("submitCode").disabled = false;
	
	if (view == 2) {
		input =	document.getElementById("imgDecode");
		preview = document.getElementById("previewDecode");

		document.getElementById("submitDecode").disabled = false;
	}
	
	if (input !== null && preview !== null) {		
		var OFReader = new FileReader();
		OFReader.readAsDataURL(input.files[0]);

		OFReader.onload = function (OFREvent) {
            preview.src = OFREvent.target.result;
        };
	}
}
