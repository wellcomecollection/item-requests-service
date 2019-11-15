package uk.ac.wellcome.platform.stacks.common.models

//return { label: 'Available', id: 'available' }
//  case 'm':
//  return { label: 'Missing', id: 'missing' }
//  case 'z':
//  return { label: 'CL Returned', id: 'cl-returned' }
//  case 'o':
//  return { label: 'o', id: 'Library use only' }
//  case 'n':
//  return { label: 'Billed not paid', id: 'billed-not-paid' }
//  case '$':
//  return { label: 'Billed paid', id: 'billed-paid' }
//  case 't':
//  return { label: 'In transit', id: 'in-transit' }
//  case '!':
//  return { label: 'On holdshelf', id: 'on-holdshelf' }
//  case 'l':
//  return { label: 'Lost', id: 'lost' }

case class StacksItemStatus(statusId: String, label: String)
