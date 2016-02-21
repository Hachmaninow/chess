var init = function () {
  game = new Chess();

  var statusEl = $('#status'),
      fenEl = $('#fen'),
      pgnEl = $('#pgn');

  // do not pick up pieces if the game is over
  // only pick up pieces for the side to move
  var onDragStart = function (source, piece, position, orientation) {
    if (game.game_over() === true ||
        (game.turn() === 'w' && piece.search(/^b/) !== -1) ||
        (game.turn() === 'b' && piece.search(/^w/) !== -1)) {
      return false;
    }
  };

  var onDrop = function (source, target) {
    // see if the move is legal
    var move = game.move({
      from: source,
      to: target,
      promotion: 'q' // NOTE: always promote to a queen for example simplicity
    });

    // illegal move
    if (move === null) return 'snapback';

    updateStatus(move);
  };

  // update the board position after the piece snap
  // for castling, en passant, pawn promotion
  var onSnapEnd = function () {
    board.position(game.fen());
  };

  var cfg = {
    draggable: true,
    position: 'start',
    onDragStart: onDragStart,
    onDrop: onDrop,
    onSnapEnd: onSnapEnd
  };
  board = ChessBoard('board', cfg);

  var updateStatus = function (move) {
    var status = '';

    var moveColor = 'White';
    if (game.turn() === 'b') {
      moveColor = 'Black';
    }

    // checkmate?
    if (game.in_checkmate() === true) {
      status = 'Game over, ' + moveColor + ' is in checkmate.';
    }

    // draw?
    else if (game.in_draw() === true) {
      status = 'Game over, drawn position';
    }

    // game still on?
    else {
      status = moveColor + ' to move';

      // check?
      if (game.in_check() === true) {
        status += ', ' + moveColor + ' is in check';
      }
    }

    statusEl.html(status);
    fenEl.html(game.fen());
    pgnEl.html(game.pgn());

    if (move != null) {
      chessdojo.core.insert_move(move);
    }
  };

  updateStatus(null);

  //$("body #notation move").click(function(){
  //  var fen = $(this).attr("fen");
  //  board.position(fen, false);
  //  var r = game.load(fen);
  //  console.log("load: " + r);
  //
  //  console.log("validate: " + game.validate_fen(fen).error)
  //});

  $("#comment-editor").dialog({
    dialogClass: "no-close",
    autoOpen: false,
    width: 400,
    height: 400,
    modal: true,
    position: {at: "center", of: "#board"},
    buttons: {
      "Ok": function () {
        chessdojo.core.set_comment($("#comment-textarea").val());
        $('#comment-editor').dialog('close')
      },
      "Cancel": function () {
        $('#comment-editor').dialog('close')
      }
    },
    //close: function () {
    //  form[0].reset();
    //}
  });

};

function updateBoard(fen) {
  board.position(fen, false);
  game.load(fen);
}

$(document).ready(init);