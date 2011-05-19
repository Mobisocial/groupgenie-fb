////
// zoned
var Timezone = {
  set : function() {
    var date = new Date()
    var timezone = "timezone=" + -date.getTimezoneOffset() * 60
    date.setTime(date.getTime() + (1000*24*60*60*1000))
    var expires = "; expires=" + date.toGMTString()
    document.cookie = timezone + expires + "; path=/; domain=.famspam.com"
  }
}

////
// $('element').scrollTo()
// $('element').scrollTo(speed)
$.fn.scrollTo = function(speed) {
  var offset = $(this).offset().top - 30
  $('html,body').animate({scrollTop: offset}, speed || 1000)
  return this
}

////
// $('element').spin()
$.fn.spin = function(append) {
  if (append)
    $(this).append('<img src="/images/spinner.gif"/>')
  else
    $(this).after('<img src="/images/spinner.gif"/>')
}

////
// generic error and success faceboxes
$.errorBox = function(text) {
  $.facebox('<h2 class="title icon_warning">Whoops</h2>' + text)
}

$.successBox = function(text) {
  $.facebox('<h2 class="title icon_check">Success</h2>' + text)
}

////
// Callback receives responseText and 'success' / 'error'
// based on response.
//
// settings hash:
//   facebox: true        // a facebox 'loading' will open pre-submit
//   confirmation: string // a confirm pop-up will open with the supplied string
//
$.fn.spamjax = function(callback, settings) {
  var settings = settings || {}
  var options  = {}

  options.complete = function(xhr, ok) { callback.call(this, xhr.responseText, ok) }

  if (settings.confirmation) {
    options.beforeSubmit = function() {
      var execute = confirm(settings.confirmation)
      if (!execute) return false
      if (settings.facebox) $.facebox.loading()
    }
  } else if (settings.facebox) { 
    options.beforeSubmit = $.facebox.loading
  }

  // TODO: test this, yo
  $(this).ajaxForm($.extend(settings, options))
  return this
}

////
// Behaviors
$(document).ready(function() {
  ////
  // new conversation scroller
  $('.jump_to_new_conversation').click(function() {
    $('#new-conversation').scrollTo().queue(function() {
      $('#message_subject').focus()
    })
    return false
  })

  ////
  // reply to conversations scroller
  $('.reply_to_conversation').click(function() {
    $('#new_message').scrollTo().queue(function() {
      $('#message_body').focus()
    })
    return false
  })

  ////
  // add another attachment
  $('#add_attachment').click(function() {
    var input = '<li><input name="attachment[]" size="30" type="file" /></li>'
    $(input).appendTo('#new-attachments')
    if ($('#add_attachment').text() == 'Attach a file') {
      $('#add_attachment').text('Attach another file')
    }
    return false
  })

  ////
  // ajax invite sidebar
  $('.reset_invite_form').click(function() {
    $('#new_person').resetForm()
    $('#invite_another').hide()
    $('#invite_another > span').remove()
    $('#new_person').show()
    return false
  })

  ////
  // invitation form
  $('#new_person').spamjax(function(res, status) {
    if (status == 'success') {
      var email = $('#person_name').val()
      $('#invite_another').prepend('<span>' + email + ' has been added!</span>')
      $('#invite_error').hide()
      $('#new_person').hide(300, function() { $('#invite_another').show(300) })
    } else {
      $('#invite_error').show().text(res)
    }
  })

  ////
  // preview invitation
  function previewInvitationLink(url) {
    var person   = {}
    person.email = $('#person_email').val()
    person.name  = $('#person_name').val()

    if (!(person.email && person.name)) {
      $('#invite_error').show().text('Please enter an email and a name.')
      return false
    } else {
      $('#invite_error').hide()
    }

    $.facebox(function() {
      $.get(url, person, function(data) {
        $.facebox('<div class="dynamic_popup">' + data + '</div>')
      })
    })

    return false
  }

  $('#preview_invitation_link').click(function() {
    return previewInvitationLink('/people/preview_invite')
  })

  $('#demo_invitation_link').click(function() {
    return previewInvitationLink('/demo/preview_invite')
  })

  ////
  // avatar upload
  $('.avatar_select').change(function() {
    $(this).spin()
    var form = this.form
    setTimeout(function() { form.submit() }, 10)
  })

  ////
  // sidebar reminder day
  $('#reminder_form').spamjax(function() { $('#reminder_update').show() } )
  $('#family_reminder_day').change(function() { $('#reminder_form').submit() })

  ////
  // repeated text folding
  $('.unfold').click(function() {
    if ($(this).text().match(/show/i))
      $(this).text('- Hide repeated text -')
    else
      $(this).text('- Show repeated text -')

    $(this).parent().next().toggle()
    return false
  })
  

  ////
  // person box tabs
  $('.person-tabs a').click(function() {
    var boss = $(this).parent().parent().parent()
    boss.find('.person-tabs li').removeClass('on')
    $(this).parent().addClass('on')
    boss.find('.person-panel').hide()
    boss.find('.person-' + $(this).text().toLowerCase()).show()
    return false
  })

  ////
  // email person's password 
  $('.email_password').click(function() {
    if (!confirm('Are you sure you want to send this person their password?  If you recently invited them, we already sent an email with a password - they should get it soon.')) return false
    var person_email = $(this).parents('li').find('.person_email').val()

    $.facebox(function() {
      $.post('/password', { email: person_email }, function(data) {
        $.facebox('<div class="dynamic_popup">' + data + '</div>')
      })
    })
    return false
  })

  $('.demo_email_password').click(function() {
    $.facebox('This would normally be how you re-send your loved one their login information.')
    return false
  })

  $('.demo_delete_person_form').click(function() {
    $.facebox('This would normally be how you remove someone from your family.')
    return false
  })

  ////
  // remove email 
  var removeEmailHandler = function() {
    $('.remove_email').click(function() {
      // silence stupid errors
      try { $(this).parent().remove() } catch(e) { }
      return false
    })
  }

  removeEmailHandler()

  ////
  // add email 
  $('.add_email').click(function() {
    $(this).parent().nextAll('.radio').before( '<li><label for="person_email">Alternate Email</label> <input id="person[emails][]" name="person[emails][]" size="30" type="text" value="" /><a href="#" class="remove_email"> <img src="/images/icons/tiny/x.png"/> </a> </li>' )
    removeEmailHandler()
    return false
  })

  ////
  // update person's profile settings
  $('.person_form').spamjax(function(data, status) {
    if (status == 'success') {
      $.successBox(data)
    } else {
      $.errorBox(data)
    }
  }, { facebox: true })

  ////
  // delete family member
  $('.delete_person_form').spamjax(function(data, status) {
    if (status == 'success') {
      $.successBox(data)
      var id = $(this).attr('url').split('/').pop()
      $('#person_' + id).hide()
    } else {
      $.errorBox(data)
    }
  }, { facebox: true, confirmation: "Are you positive you want to delete this person? You will not be able to add them back into the family." })

  ////
  // signup helpers
  $('#signup-terms').click(function() {
    $('#signup-button').attr('disabled', !this.checked)
  })

  $('#signup-button').click(function() {
    $(this).attr('disabled', true)
    $(this).val('Creating your FamSpam site...')
    $('#signup_form').submit()
  })
  
  ////
  // plans helpers
  $('#plans :radio').change(function() {
    $('#plans tr').removeClass('selected')
    $(this).parent().parent().addClass('selected') 
  })
  
  $('#plans tr').css('cursor', 'pointer').click(function() {
    $(this).find(':radio').select().change()
  })

  $('#plans :checked').parent().parent().addClass('selected')

  ////
  // try it / buy it helper
  $('#signup_widget .form, #signup_widget, #promo').corner()

  ////
  // login helpers
  $('#login_form').submit(Timezone.set)

  $('#quick_signup').spamjax(function(data, status) {
    if (status == 'success') {
      $('#signup_widget').find('input, :submit').attr('disabled', true)
      data = $('#quick_signup_success').clone()
    } else {
      data = $('#quick_signup_error').clone()
    }

    $('#signup_widget .left').empty().append(data.show())

  }, { beforeSubmit: function() {
      $('#signup_widget .left').empty().removeClass('arrow_bg').spin(true)
  }})

  ////
  // corner
  $('.corner').corner()

  ////
  // lightbox activation
  if ($.facebox) 
    $('a[rel*=facebox]').facebox({
      next_image    : '/images/icons/small/fast_forward.png',
      play_image    : '/images/icons/small/play.png',
      pause_image   : '/images/icons/small/pause.png',
      prev_image    : '/images/icons/small/rewind.png'
    }) 
});

////
// add Accept:text/javascript header to jQuery ajax requests
$.ajaxSetup({ 'beforeSend': function(xhr) {xhr.setRequestHeader("Accept", "text/javascript")} })
