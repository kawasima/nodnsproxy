#!/usr/local/bin/ruby

require 'resolv'

name = ENV['PATH_INFO']
if name.nil? or name[0] != "/"[0]
  puts "Status: 404 Not Found"
  puts ""
  exit 0
end

begin
  name = name[1..255]
  address = Resolv.getaddress(name)
  puts "content-type: text/plain"
  puts ""
  puts address
rescue
  puts "Status: 404 Not Found"
  puts ""
end
